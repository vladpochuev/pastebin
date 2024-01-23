package com.vladpochuev.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.BinEntity;
import com.vladpochuev.model.BinMessage;
import com.vladpochuev.service.FirestoreMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@Controller
public class MapController {
    private final BinDAO binDAO;
    private final FirestoreMessageService messageService;

    @Autowired
    public MapController(BinDAO binDAO, FirestoreMessageService messageService) {
        this.binDAO = binDAO;
        this.messageService = messageService;
    }

    @GetMapping("/")
    public String redirectIntoMap() {
        return "redirect:/map";
    }

    @GetMapping("/map")
    public String getMenu(Model model, @RequestParam(value = "id", required = false) String id) {
        BinEntity selectedBinEntity = this.binDAO.readById(id);

        ResponseEntity<BinMessage> urlBin = id == null ? null : defineMessage(selectedBinEntity);
        model.addAttribute("urlBin", urlBin);
        model.addAttribute("bins", getBinEntities());
        return "map";
    }

    private String getBinEntities() {
        try {
            List<BinEntity> binEntities = this.binDAO.read();
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(binEntities);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ResponseEntity<BinMessage> defineMessage(BinEntity selectedBinEntity) {
        if (selectedBinEntity != null) {
            BinMessage message = BinMessage.getFromBinEntity(selectedBinEntity, this.messageService);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
