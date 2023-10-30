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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/map")
public class MainController {
    private final BinDAO binDAO;

    @Autowired
    public MainController(BinDAO binDAO) {
        this.binDAO = binDAO;
    }

    @GetMapping("")
    public String getMenu(Model model, @RequestParam(value = "id", required = false) String id) {
        Bin selectedBin = binDAO.readById(id);

        BinMessage urlBin = id == null ? null : defineMessage(selectedBin);
        model.addAttribute("urlBin", urlBin);

        try {
            List<Bin> bins = binDAO.readAll();
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
        Bin bin = binDAO.readById(id);
        return new ResponseEntity<>(defineMessage(bin), HttpStatus.OK);
    }

    private BinMessage defineMessage(Bin selectedBin) {
        BinMessage message;
        if(selectedBin != null) {
            message = BinMessage.getFromBin(selectedBin, StatusCode.OK);
        } else {
            message = BinMessage.getFromBin(new Bin(), StatusCode.ERROR);
        }
        return message;
    }

    @MessageMapping("/newBin")
    @SendTo("/topic/binNotifications")
    public BinNotification sendBinWS(Bin bin) {
        HashGenerator hashGenerator = new HashGenerator(bin.getTitle(), System.nanoTime(), Math.floor(Math.random() * 1024));
        bin.setId(hashGenerator.getHash("SHA-1", 10));

        try {
            if (bin.getX() == null && bin.getY() == null) {
                createBinWithBFS(bin);
            } else {
                binDAO.create(bin);
            }
        } catch (Exception e) {
            return BinNotification.getFromBin(bin, StatusCode.ERROR);
        }

        LinkHandler handler = new LinkHandler(bin.getId(), bin.getAmountOfTime(), binDAO);
        handler.start();
        return BinNotification.getFromBin(bin, StatusCode.OK);
    }

    private void createBinWithBFS(Bin bin) {
        BFS<Bin> bfs = new BFS<>(binDAO, 100, 100);
        while (true) {
            try {
                Point coords = bfs.findNearest();
                bin.setX(coords.getX());
                bin.setY(coords.getY());
                binDAO.create(bin);
                break;
            } catch (DuplicateKeyException ignored) {}
        }
    }
}
