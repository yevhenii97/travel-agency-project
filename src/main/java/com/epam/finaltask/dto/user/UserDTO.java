package com.epam.finaltask.dto.user;

import java.util.List;

import com.epam.finaltask.model.entities.Voucher;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;

    @NotBlank(message = "{user.username.required}")
    @Size(
            min = 3,
            max = 50,
            message = "{user.username.size}"
    )
    private String username;

    @NotBlank(message = "{user.password.required}")
    @Size(
            min = 8,
            message = "{user.password.size}"
    )
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "{user.password.pattern}"
    )
    private String password;

    @NotBlank(message = "{user.role.required}")
    private String role;

    private List<Voucher> vouchers;

    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "{user.phone.pattern}"
    )
    private String phoneNumber;

    @NotNull(message = "{user.balance.required}")
    @PositiveOrZero(message = "{user.balance.positive}"
    )
    private Double balance;

    @NotNull(message = "{user.active.required}")
    private Boolean isActive;
}
