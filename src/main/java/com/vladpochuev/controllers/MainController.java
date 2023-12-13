package com.vladpochuev.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.Bin;
import com.vladpochuev.model.BinMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/")
    public String redirectIntoMap() {
        return "redirect:/map";
    }

    @GetMapping("/map")
    public String getMenu(Model model, @RequestParam(value = "id", required = false) String id) {
        Bin selectedBin = binDAO.readById(id);

        ResponseEntity<BinMessage> urlBin = id == null ? null : defineMessage(selectedBin);
        model.addAttribute("urlBin", urlBin);

        try {
            List<Bin> bins = binDAO.read();
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(bins);
            String byteString = Base64.getEncoder().encodeToString(bytes);
            model.addAttribute("bins", byteString);
            return "map";
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/login")
    public String getLoginPage(@RequestParam(value = "binToCreate", required = false) String bin, Model model) {
        model.addAttribute("binToCreate", bin);
        return "/login";
    }

    private ResponseEntity<BinMessage> defineMessage(Bin selectedBin) {
        if(selectedBin != null) {
            return new ResponseEntity<>(BinMessage.getFromBin(selectedBin), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
