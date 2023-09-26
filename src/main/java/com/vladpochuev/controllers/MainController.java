package com.vladpochuev.controllers;

import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.Bin;
import com.vladpochuev.service.HashGenerator;
import org.springframework.beans.factory.annotation.Autowired;
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
        return "main";
    }

    @PostMapping()
    public String submitBin(@ModelAttribute("bin") Bin bin) {
        HashGenerator hashGenerator = new HashGenerator(bin.getTitle(), bin.getText());
        bin.setId(hashGenerator.getHash("SHA-1", 10));
        binDAO.create(bin);
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
}
