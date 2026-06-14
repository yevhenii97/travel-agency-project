package com.epam.finaltask.dto.voucher;

import com.epam.finaltask.annotations.ValidVoucherDates;
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
@ValidVoucherDates
public class VoucherDTO {

    private String id;

    @NotBlank(message = "{voucher.title.required}")
    @Size(
            min = 3,
            max = 100,
            message = "{voucher.title.size}"
    )
    private String title;

    @NotBlank(message = "{voucher.description.required}")
    @Size(
            max = 1000,
            message = "{voucher.description.size}"
    )
    private String description;

    @NotNull(message = "{voucher.price.required}")
    @Positive(message = "{voucher.price.positive}")
    private Double price;

    @NotBlank(message = "{voucher.tourType.required}")
    private String tourType;

    @NotBlank(message = "{voucher.transferType.required}")
    private String transferType;

    @NotBlank(message = "{voucher.hotelType.required}")
    private String hotelType;

    @NotBlank(message = "{voucher.status.required}")
    private String status;

    @NotNull(message = "{voucher.arrivalDate.required}")
    @Future(message = "{voucher.arrivalDate.future}")
    private LocalDate arrivalDate;

    @NotNull(message = "{voucher.evictionDate.required}")
    @Future(message = "{voucher.evictionDate.future}")
    private LocalDate evictionDate;

    private UUID userId;

    @NotNull(message = "{voucher.hot.required}")
    private Boolean isHot;
}
