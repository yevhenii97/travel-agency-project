package com.epam.finaltask.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class VoucherDTO {

    private String id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotBlank(message = "Tour type is required")
    private String tourType;

    @NotBlank(message = "Transfer type is required")
    private String transferType;

    @NotBlank(message = "Hotel type is required")
    private String hotelType;

    @NotBlank(message = "Status is required")
    private String status;

    @NotNull(message = "Arrival date is required")
//    @Future(message = "Arrival date must be in the future")
    private LocalDate arrivalDate;

    @NotNull(message = "Eviction date is required")
//    @Future(message = "Eviction date must be in the future")
    private LocalDate evictionDate;

    private UUID userId;

    @NotNull(message = "Hot status is required")
    private Boolean isHot;
}
