package com.vladpochuev.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("")
    public String redirectIntoMap() {
        return "redirect:/map";
    }
}
