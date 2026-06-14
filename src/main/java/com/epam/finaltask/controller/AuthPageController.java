package com.epam.finaltask.controller;

import com.epam.finaltask.dto.auth.LoginRequest;
import com.epam.finaltask.dto.auth.RegisterRequest;
import com.epam.finaltask.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthPageController {

    private final AuthService authService;

    @GetMapping("/sign-in")
    public String signIn(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/sign-in";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/sign-up";
    }

    @PostMapping("/sign-up")
    public String register(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/sign-up";
        }

        try {
            authService.register(registerRequest);
            return "redirect:/auth/sign-in?registered=true";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            return "auth/sign-up";
        }
    }
}
