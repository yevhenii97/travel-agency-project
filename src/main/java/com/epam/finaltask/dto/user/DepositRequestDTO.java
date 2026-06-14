package com.epam.finaltask.dto.user;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequestDTO {
    @DecimalMin(value = "0.01")
    @NotNull
    private BigDecimal amount;
}
