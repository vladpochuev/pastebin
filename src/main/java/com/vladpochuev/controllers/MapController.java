package com.vladpochuev.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.Bin;
import com.vladpochuev.model.BinMessage;
import com.vladpochuev.model.BinNotification;
import com.vladpochuev.model.Point;
import com.vladpochuev.service.BFS;
import com.vladpochuev.service.HashGenerator;
import com.vladpochuev.service.LinkHandler;
import com.vladpochuev.service.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/map")
public class MapController {
    private final BinDAO binDAO;
    private final LinkHandler linkHandler;

    @Autowired
    public MapController(BinDAO binDAO, LinkHandler linkHandler) {
        this.binDAO = binDAO;
        this.linkHandler = linkHandler;
    }

    @GetMapping("")
    public String getMenu(Model model, @RequestParam(value = "id", required = false) String id) {
        Bin selectedBin = binDAO.readById(id);

        BinMessage urlBin = id == null ? null : defineMessage(selectedBin);
        model.addAttribute("urlBin", urlBin);

        try {
            List<Bin> bins = binDAO.read();
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(bins);
            String byteString = Base64.getEncoder().encodeToString(bytes);
            model.addAttribute("bins", byteString);
            return "index";
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/bin")
    public ResponseEntity<BinMessage> getBin(@RequestParam("id") String id) {
        try {
            Bin bin = binDAO.readById(id);
            return new ResponseEntity<>(defineMessage(bin), HttpStatus.OK);
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return new ResponseEntity<>(BinMessage.getFromBin(new Bin(), StatusCode.SERVER_ERROR), HttpStatus.OK);
        }
    }

    private BinMessage defineMessage(Bin selectedBin) {
        BinMessage message;
        if(selectedBin != null) {
            message = BinMessage.getFromBin(selectedBin, StatusCode.OK);
        } else {
            message = BinMessage.getFromBin(new Bin(), StatusCode.NO_SUCH_BIN);
        }
        return message;
    }

    @MessageMapping("/createBin")
    @SendTo("/topic/createdBinNotifications")
    public BinNotification createBinWS(Bin bin) {
        HashGenerator hashGenerator = new HashGenerator(bin.getTitle(), System.nanoTime(), Math.floor(Math.random() * 1024));
        bin.setId(hashGenerator.getHash("SHA-1", 10));

        String expirationTime = linkHandler.getExpirationTime(bin.getAmountOfTime());
        bin.setExpirationTime(expirationTime);

        try {
            if (bin.getX() == null && bin.getY() == null) {
                createBinWithBFS(bin);
            } else {
                binDAO.create(bin);
            }
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return BinNotification.getFromBin(bin, StatusCode.SERVER_ERROR);
        } catch (DuplicateKeyException e) {
            return BinNotification.getFromBin(bin, StatusCode.DUPLICATE);
        }

        return BinNotification.getFromBin(bin, StatusCode.OK);
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
    public BinNotification deleteBinWS(String id) {
        Bin bin;
        try {
            bin = binDAO.readById(id);
            if(bin != null) {
                binDAO.delete(id);
            } else {
                return BinNotification.getFromBin(new Bin(), StatusCode.NO_SUCH_BIN);
            }
        } catch (DataAccessResourceFailureException | QueryTimeoutException e) {
            return BinNotification.getFromBin(new Bin(), StatusCode.SERVER_ERROR);
        }
        return BinNotification.getFromBin(bin, StatusCode.OK);
    }
}
