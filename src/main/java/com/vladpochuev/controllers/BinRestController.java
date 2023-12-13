package com.vladpochuev.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.Bin;
import com.vladpochuev.model.BinMessage;
import com.vladpochuev.model.BinNotification;
import com.vladpochuev.model.Point;
import com.vladpochuev.security.TokenCookieSessionAuthenticationStrategy;
import com.vladpochuev.service.BFS;
import com.vladpochuev.service.HashGenerator;
import com.vladpochuev.service.LinkHandler;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BinRestController {
    private final BinDAO binDAO;
    private final LinkHandler linkHandler;

    public BinRestController(BinDAO binDAO, LinkHandler linkHandler) {
        this.binDAO = binDAO;
        this.linkHandler = linkHandler;
    }

    @GetMapping("/bin")
    public ResponseEntity<BinMessage> getBin(@RequestParam("id") String id) {
        try {
            Bin bin = binDAO.readById(id);
            return defineMessage(bin);
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<BinMessage> defineMessage(Bin selectedBin) {
        if(selectedBin != null) {
            return new ResponseEntity<>(BinMessage.getFromBin(selectedBin), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @MessageMapping({"/createBin"})
    @SendTo("/topic/createdBinNotifications")
    public ResponseEntity<BinNotification> createBinWS(Bin bin, Principal principal, StompHeaderAccessor accessor) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        String clientId = accessor.getFirstNativeHeader("clientId");
        headers.add("clientId", clientId);

        if(principal == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(bin);
            headers.add("binToCreate", Base64.getEncoder().encodeToString(json.getBytes()));
            return new ResponseEntity<>(headers, HttpStatus.UNAUTHORIZED);
        }

        HashGenerator hashGenerator = new HashGenerator(bin.getTitle(), System.nanoTime(), Math.floor(Math.random() * 1024));
        bin.setId(hashGenerator.getHash("SHA-1", 10));

        String expirationTime = linkHandler.getExpirationTime(bin.getAmountOfTime());
        bin.setExpirationTime(expirationTime);
        bin.setUsername(principal.getName());

        try {
            if (bin.getX() == null && bin.getY() == null) {
                createBinWithBFS(bin);
            } else {
                binDAO.create(bin);
            }
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DuplicateKeyException e) {
            return new ResponseEntity<>(headers, HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(BinNotification.getFromBin(bin), headers, HttpStatus.OK);
    }

    private void createBinWithBFS(Bin bin) {
        while (true) {
            BFS<Bin> bfs = new BFS<>(binDAO);
            bfs.fillField();
            try {
                Point coords = bfs.findNearest();
                bin.setX(coords.getX());
                bin.setY(coords.getY());
                binDAO.create(bin);
                break;
            } catch (DuplicateKeyException ignored) {}
        }
    }

    @MessageMapping("/deleteBin")
    @SendTo("/topic/deletedBinNotifications")
    public ResponseEntity<BinNotification> deleteBinWS(String id, Principal principal, StompHeaderAccessor accessor) {
        HttpHeaders headers = new HttpHeaders();
        String clientId = accessor.getFirstNativeHeader("clientId");
        headers.add("clientId", clientId);

        if(principal == null) {
            headers.add("binToDelete", id);
            return new ResponseEntity<>(headers, HttpStatus.UNAUTHORIZED);
        }

        Bin bin;
        try {
            bin = binDAO.readById(id);

            if(bin == null) return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);

            if(!bin.getUsername().equals(principal.getName())) {
                return new ResponseEntity<>(headers, HttpStatus.FORBIDDEN);
            }
            binDAO.delete(id);
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(BinNotification.getFromBin(bin), headers, HttpStatus.OK);
    }
}
