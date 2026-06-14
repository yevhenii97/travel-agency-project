package com.epam.finaltask.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TransactionsDTO {

    private String type;
    private Double amount;
    private LocalDateTime createdAt;

}
