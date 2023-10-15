package com.vladpochuev.controllers;

import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.Bin;
import com.vladpochuev.model.BinMessage;
import com.vladpochuev.model.BinNotification;
import com.vladpochuev.service.HashGenerator;
import com.vladpochuev.service.LinkHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {
    private final BinDAO binDAO;
    @Autowired
    public MainController(BinDAO binDAO) {
        this.binDAO = binDAO;
    }

    @GetMapping()
    public String getMenu(Model model) {
        model.addAttribute("bin", new Bin());
        return "index";
    }

    @PostMapping()
    public String submitBin(@ModelAttribute("bin") Bin bin,
                            @ModelAttribute("amountOfTime") String amountOfTime) {
        HashGenerator hashGenerator = new HashGenerator(bin.getTitle(), bin.getText());
        bin.setId(hashGenerator.getHash("SHA-1", 10));
        binDAO.create(bin);
        LinkHandler handler = new LinkHandler(bin.getId(), amountOfTime, binDAO);
        handler.start();
        return "redirect:/" + bin.getId();
    }

    @GetMapping("/{id}")
    public String getBin(@PathVariable String id, Model model) {
        Bin bin = binDAO.read(id);
        model.addAttribute("bin", bin);
        return "get_bin";
    }

    @PostMapping("/redirected")
    public String getHash(@RequestParam("hash") String hash) {
        return "redirect:/" + hash;
    }

    @MessageMapping("/newBin")
    @SendTo("/topic/binNotifications")
    public BinNotification sendBinWS(BinMessage message) {
        return new BinNotification(message.getX(), message.getY());
    }
}
