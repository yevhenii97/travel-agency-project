package com.epam.finaltask.controller;

import com.epam.finaltask.dto.user.ChangeVoucherStatusRequestDTO;
import com.epam.finaltask.dto.user.UserResponseDTO;
import com.epam.finaltask.dto.voucher.ChangeHotStatusRequestDTO;
import com.epam.finaltask.dto.voucher.VoucherDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class VoucherPageController {

    private final VoucherService voucherService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model
    ) {
        UserResponseDTO currentUser =
                userService.getUserByUsername(authentication.getName());

        List<VoucherDTO> vouchers;

        if ("USER".equals(currentUser.getRole())) {
            vouchers = voucherService.findAvailable(PageRequest.of(0, 100));
        } else {
            vouchers = voucherService.findAll(PageRequest.of(0, 100));
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("vouchers", vouchers);

        return "user/dashboard";
    }

    @PostMapping("/vouchers/{id}/order")
    @PreAuthorize("hasRole('USER')")
    public String orderVoucher(
            @PathVariable String id,
            Authentication authentication
    ) {
        try {
            voucherService.order(id, authentication.getName());
            return "redirect:/vouchers/my?success=true";
        } catch (ResponseStatusException ex) {
            return "redirect:/dashboard?error=true";
        }
    }

    @GetMapping("/vouchers/my")
    @PreAuthorize("hasRole('USER')")
    public String myVouchers(
            Authentication authentication,
            Model model
    ) {
        model.addAttribute(
                "vouchers",
                voucherService.getCurrentUserVouchers(
                        authentication.getName(),
                        PageRequest.of(0, 100)
                )
        );

        return "vouchers/my-vouchers";
    }

    @PostMapping("/manager/vouchers/{id}/hot")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String changeHotStatus(@PathVariable String id) {
        ChangeHotStatusRequestDTO request = new ChangeHotStatusRequestDTO();
        request.setHot(true);

        voucherService.changeHotStatus(id, request);

        return "redirect:/dashboard?success=true";
    }

    @PostMapping("/manager/vouchers/{id}/paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String markAsPaid(@PathVariable String id) {
        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("PAID");

        voucherService.changeStatus(id, request);

        return "redirect:/dashboard?success=true";
    }

    @PostMapping("/manager/vouchers/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String cancelVoucher(@PathVariable String id) {
        ChangeVoucherStatusRequestDTO request = new ChangeVoucherStatusRequestDTO();
        request.setStatus("CANCELED");

        voucherService.changeStatus(id, request);

        return "redirect:/dashboard?success=true";
    }
}
