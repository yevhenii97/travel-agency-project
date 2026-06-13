package com.epam.finaltask.dto;

import java.util.List;

import com.epam.finaltask.model.Voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDTO {

	private String id;

    @NotBlank(message = "Username is required")
	private String username;

    @Size(min = 8, message = "Password must be at least 8 characters")
	private String password;
	private String role;
	private List<Voucher> vouchers;

    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number is invalid"
    )
	private String phoneNumber;

    @PositiveOrZero(message = "Balance must be zero or positive")
	private Double balance;
	private boolean active;
}
