package com.epam.finaltask.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {

    @NotBlank(message = "{user.oldPassword.required}")
    private String oldPassword;

    @NotBlank(message = "{user.newPassword.required}")
    @Size(min = 8, message = "{user.password.size}")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "{user.password.pattern}"
    )
    private String newPassword;
}
