package com.vladpochuev.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.Bin;
import com.vladpochuev.model.BinMessage;
import com.vladpochuev.model.BinNotification;
import com.vladpochuev.service.HashGenerator;
import com.vladpochuev.service.LinkHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@Controller
public class MainController {
    private final BinDAO binDAO;
    @Autowired
    public MainController(BinDAO binDAO) {
        this.binDAO = binDAO;
    }

    @GetMapping()
    public String getMenu(Model model) {
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
        Bin bin = binDAO.read(id);
        return new ResponseEntity<>(new BinMessage(bin.getId(), bin.getTitle(), bin.getMessage(), bin.getX(), bin.getY()), HttpStatus.OK);
    }

    @MessageMapping("/newBin")
    @SendTo("/topic/binNotifications")
    public BinNotification sendBinWS(Bin bin) {
        HashGenerator hashGenerator = new HashGenerator(bin.getTitle(), bin.getMessage());
        bin.setId(hashGenerator.getHash("SHA-1", 10));
        binDAO.create(bin);
        LinkHandler handler = new LinkHandler(bin.getId(), bin.getAmountOfTime(), binDAO);
        handler.start();
        return new BinNotification(bin.getId(), bin.getTitle(), bin.getX(), bin.getY());
    }
}
