package com.epam.finaltask.controller;

import com.epam.finaltask.dto.user.ChangePasswordRequestDTO;
import com.epam.finaltask.dto.user.DepositRequestDTO;
import com.epam.finaltask.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserPageController {

    private final UserService userService;

    @GetMapping("/me/deposit")
    @PreAuthorize("hasRole('USER')")
    public String depositPage(Model model) {
        model.addAttribute("depositRequest", new DepositRequestDTO());
        return "user/deposit";
    }

    @PostMapping("/me/deposit")
    @PreAuthorize("hasRole('USER')")
    public String depositBalance(
            Authentication authentication,
            @ModelAttribute DepositRequestDTO request
    ) {
        userService.depositOwnBalance(authentication.getName(), request);
        return "redirect:/dashboard?success=true";
    }

    @GetMapping("/me/password")
    @PreAuthorize("hasRole('USER')")
    public String changePasswordPage(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequestDTO());
        return "user/change-password";
    }

    @PostMapping("/me/password")
    @PreAuthorize("hasRole('USER')")
    public String changePassword(
            Authentication authentication,
            @ModelAttribute ChangePasswordRequestDTO request
    ) {
        userService.changePassword(authentication.getName(), request);
        return "redirect:/dashboard?success=true";
    }
}
