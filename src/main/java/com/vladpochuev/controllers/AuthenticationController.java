package com.vladpochuev.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthenticationController {
    @GetMapping("/login")
    public String getLoginPage(@RequestParam(value = "binToCreate", required = false) String bin, Model model) {
        model.addAttribute("binToCreate", bin);
        return "/login";
    }

    @GetMapping("/signup")
    public String getRegistrationPage(Model model, @RequestParam(value = "binToCreate", required = false) String bin) {
        model.addAttribute("binToCreate", bin);
        return "/registration";
    }

    @GetMapping("/logout")
    public String getLogoutPage() {
        return "/logout";
    }
}
