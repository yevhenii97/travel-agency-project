package com.epam.finaltask.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequestDTO {
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "{user.phone.pattern}"
    )
    private String phoneNumber;
}
