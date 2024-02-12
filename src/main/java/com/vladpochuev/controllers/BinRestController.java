package com.vladpochuev.controllers;

import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.*;
import com.vladpochuev.dao.BinDaoBFS;
import com.vladpochuev.service.FirestoreMessageService;
import com.vladpochuev.service.HashGenerator;
import com.vladpochuev.service.LinkHandler;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@SessionAttributes("userData")
@RequestMapping("/api")
public class BinRestController {
    private final BinDAO binDAO;
    private final LinkHandler linkHandler;
    private final FirestoreMessageService firestoreMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public BinRestController(BinDAO binDAO, LinkHandler linkHandler, FirestoreMessageService firestoreMessageService,
                             SimpMessagingTemplate messagingTemplate) {
        this.binDAO = binDAO;
        this.linkHandler = linkHandler;
        this.firestoreMessageService = firestoreMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/bin")
    public ResponseEntity<BinMessage> getBin(String id) {
        try {
            BinEntity binEntity = this.binDAO.readById(id);
            return defineMessage(binEntity);
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<BinMessage> defineMessage(BinEntity selectedBinEntity) {
        if (selectedBinEntity != null) {
            BinMessage message = BinMessage.getFromBinEntity(selectedBinEntity, this.firestoreMessageService);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/bin")
    public ResponseEntity<BinNotification> createBin(Bin bin, Principal principal) {
        BinEntity binEntity = convertBinToEntity(bin, principal);
        HttpStatus receivedStatus = createBin(binEntity);
        BinNotification notification = BinNotification.getFromBinEntity(binEntity);
        if (receivedStatus == HttpStatus.OK) {
            FirestoreMessageEntity message = new FirestoreMessageEntity(binEntity.getMessageUUID(), bin.getMessage());
            this.firestoreMessageService.sendCreate(message);
            this.messagingTemplate.convertAndSend("/topic/createdBinNotifications",
                    new ResponseEntity<>(notification, receivedStatus));
        }
        return new ResponseEntity<>(notification, receivedStatus);
    }

    @DeleteMapping("/bin")
    public ResponseEntity<BinNotification> deleteBin(String id, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        BinEntity binEntity = this.binDAO.readById(id);
        BinNotification notification = BinNotification.getFromBinEntity(binEntity);
        HttpStatus receivedStatus = deleteBin(binEntity, principal);
        if (receivedStatus == HttpStatus.OK) {
            this.firestoreMessageService.sendDelete(binEntity.getMessageUUID());
            this.messagingTemplate.convertAndSend("/topic/deletedBinNotifications",
                    new ResponseEntity<>(notification, receivedStatus));
        }
        return new ResponseEntity<>(notification, receivedStatus);
    }

    private BinEntity convertBinToEntity(Bin bin, Principal principal) {
        HashGenerator hashGenerator = new HashGenerator(bin.getTitle(), System.nanoTime(), Math.floor(Math.random() * 1024));
        String id = hashGenerator.getHash("SHA-1", 10);
        String expirationTime = this.linkHandler.getExpirationTime(bin.getAmountOfTime());
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
                this.binDAO.create(binEntity);
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
            BinDaoBFS binDaoBFS = new BinDaoBFS(this.binDAO);
            binDaoBFS.fillField();
            try {
                Point coords = binDaoBFS.findNearest();
                binEntity.setX(coords.getX());
                binEntity.setY(coords.getY());
                this.binDAO.create(binEntity);
                break;
            } catch (DuplicateKeyException ignored) {
            }
        }
    }

    private HttpStatus deleteBin(BinEntity binEntity, Principal principal) {
        try {
            if (binEntity == null) {
                return HttpStatus.NOT_FOUND;
            } else if (!binEntity.getUsername().equals(principal.getName())) {
                return HttpStatus.FORBIDDEN;
            } else {
                this.binDAO.delete(binEntity.getId());
                return HttpStatus.OK;
            }
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
