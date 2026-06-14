package com.epam.finaltask.restcontroller.controllers;

import com.epam.finaltask.dto.auth.LoginRequest;
import com.epam.finaltask.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthPageController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/sign-in";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request) {
        authService.login(request);
        return "redirect:/";
    }
}
