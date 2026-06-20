package com.epam.finaltask.controller;

import com.epam.finaltask.dto.user.ChangeUserStatusDTO;
import com.epam.finaltask.dto.user.UserResponseDTO;
import com.epam.finaltask.dto.voucher.VoucherDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPageController {

    private final UserService userService;
    private final VoucherService voucherService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageUsers(
            Model model,
            @PageableDefault(size = 20, sort = "username") Pageable pageable
    ) {
        Page<UserResponseDTO> usersPage = userService.getAllUsers(pageable);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("users", usersPage.getContent());

        return "admin/users";
    }

    @PostMapping("/users/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public String blockUser(@PathVariable String id) {
        ChangeUserStatusDTO dto = new ChangeUserStatusDTO();
        dto.setActive(false);

        userService.changeAccountStatusById(id, dto);

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public String unblockUser(@PathVariable String id) {
        ChangeUserStatusDTO dto = new ChangeUserStatusDTO();
        dto.setActive(true);

        userService.changeAccountStatusById(id, dto);

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public String makeManager(@PathVariable String id) {
        userService.setRoleManagerForUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/vouchers/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createVoucherPage(Model model) {
        model.addAttribute("voucher", new VoucherDTO());
        return "admin/create-voucher";
    }

    @PostMapping("/vouchers/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createVoucher(
            @Valid @ModelAttribute("voucher") VoucherDTO voucherDTO,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/create-voucher";
        }

        voucherService.create(voucherDTO);
        return "redirect:/dashboard?success=true";
    }

    @GetMapping("/vouchers/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editVoucherPage(
            @PathVariable String id,
            Model model
    ) {
        model.addAttribute("voucher", voucherService.findById(id));
        return "admin/edit-voucher";
    }

    @PostMapping("/vouchers/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateVoucherFromPage(
            @PathVariable String id,
            @ModelAttribute VoucherDTO voucherDTO
    ) {
        voucherService.update(id, voucherDTO);
        return "redirect:/dashboard?success=true";
    }

    @PostMapping("/vouchers/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteVoucher(@PathVariable String id) {
        voucherService.delete(id);
        return "redirect:/dashboard?success=true";
    }
}
