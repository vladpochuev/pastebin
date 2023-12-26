package com.vladpochuev.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.*;
import com.vladpochuev.service.BFS;
import com.vladpochuev.service.FirestoreMessageService;
import com.vladpochuev.service.HashGenerator;
import com.vladpochuev.service.LinkHandler;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class BinRestController {
    private final BinDAO binDAO;
    private final LinkHandler linkHandler;
    private final FirestoreMessageService messageService;

    public BinRestController(BinDAO binDAO, LinkHandler linkHandler, FirestoreMessageService messageService) {
        this.binDAO = binDAO;
        this.linkHandler = linkHandler;
        this.messageService = messageService;
    }

    @GetMapping("/bin")
    public ResponseEntity<BinMessage> getBin(@RequestParam("id") String id) {
        try {
            BinEntity binEntity = binDAO.readById(id);
            return defineMessage(binEntity);
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<BinMessage> defineMessage(BinEntity selectedBinEntity) {
        if(selectedBinEntity != null) {
            return new ResponseEntity<>(BinMessage.getFromBinEntity(selectedBinEntity, messageService), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @MessageMapping({"/createBin"})
    @SendTo("/topic/createdBinNotifications")
    public ResponseEntity<BinNotification> createBinWS(Bin bin, Principal principal, StompHeaderAccessor accessor)
            throws ExecutionException, InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("clientId", accessor.getFirstNativeHeader("clientId"));

        if(principal == null) {
            putBinToCreate(bin, headers);
            return new ResponseEntity<>(headers, HttpStatus.UNAUTHORIZED);
        }

        BinEntity binEntity = convertBinToEntity(bin, principal);
        messageService.sendCreate(new FirestoreMessageEntity(binEntity.getMessageUUID(), bin.getMessage()));

        HttpStatus receivedStatus = createBin(binEntity);
        if(receivedStatus == HttpStatus.OK) {
            return new ResponseEntity<>(BinNotification.getFromBinEntity(binEntity), headers, receivedStatus);
        } else {
            return new ResponseEntity<>(headers, receivedStatus);
        }
    }

    private void putBinToCreate(Bin bin, HttpHeaders headers) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(bin);
            headers.add("binToCreate", Base64.getEncoder().encodeToString(json.getBytes()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

    private BinEntity convertBinToEntity(Bin bin, Principal principal) {
        HashGenerator hashGenerator = new HashGenerator(bin.getTitle(), System.nanoTime(), Math.floor(Math.random() * 1024));
        String id = hashGenerator.getHash("SHA-1", 10);
        String expirationTime = linkHandler.getExpirationTime(bin.getAmountOfTime());
        String username = principal.getName();
        String messageUUID = UUID.randomUUID().toString();
        return new BinEntity(id, bin.getTitle(), messageUUID, bin.getX(), bin.getY(), bin.getColor(),
                bin.getAmountOfTime(), expirationTime, username);
    }

    private HttpStatus createBin(BinEntity binEntity) {
        try {
            if (binEntity.getX() == null && binEntity.getY() == null) {
                createBinWithBFS(binEntity);
            } else {
                binDAO.create(binEntity);
            }
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (DuplicateKeyException e) {
            return HttpStatus.CONFLICT;
        } catch (DataIntegrityViolationException e) {
            return HttpStatus.NOT_ACCEPTABLE;
        }
        return HttpStatus.OK;
    }

    private void createBinWithBFS(BinEntity binEntity) {
        while (true) {
            BFS<BinEntity> bfs = new BFS<>(binDAO);
            bfs.fillField();
            try {
                Point coords = bfs.findNearest();
                binEntity.setX(coords.getX());
                binEntity.setY(coords.getY());
                binDAO.create(binEntity);
                break;
            } catch (DuplicateKeyException ignored) {}
        }
    }

    @MessageMapping("/deleteBin")
    @SendTo("/topic/deletedBinNotifications")
    public ResponseEntity<BinNotification> deleteBinWS(String id, Principal principal, StompHeaderAccessor accessor)
            throws ExecutionException, InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("clientId", accessor.getFirstNativeHeader("clientId"));

        if(principal == null) {
            headers.add("binToDelete", id);
            return new ResponseEntity<>(headers, HttpStatus.UNAUTHORIZED);
        }

        BinEntity binEntity = binDAO.readById(id);
        HttpStatus receivedStatus = deleteBin(binEntity, principal);
        if(receivedStatus == HttpStatus.OK) {
            return new ResponseEntity<>(BinNotification.getFromBinEntity(binEntity), headers, receivedStatus);
        } else {
            return new ResponseEntity<>(headers, receivedStatus);
        }
    }

    private HttpStatus deleteBin(BinEntity binEntity, Principal principal) throws ExecutionException, InterruptedException {
        try {
            if(binEntity == null) {
                return HttpStatus.NOT_FOUND;
            } else if(!binEntity.getUsername().equals(principal.getName())) {
                return HttpStatus.FORBIDDEN;
            } else {
                binDAO.delete(binEntity.getId());
                messageService.sendDelete(binEntity.getMessageUUID());
                return HttpStatus.OK;
            }
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
