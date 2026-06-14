package com.epam.finaltask.utils;

import com.epam.finaltask.annotations.ValidVoucherDates;
import com.epam.finaltask.dto.voucher.VoucherDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VoucherDatesValidator
        implements ConstraintValidator<ValidVoucherDates, VoucherDTO> {

    @Override
    public boolean isValid(
            VoucherDTO voucher,
            ConstraintValidatorContext context
    ) {

        if (voucher.getArrivalDate() == null
                || voucher.getEvictionDate() == null) {
            return true;
        }

        return voucher.getEvictionDate()
                .isAfter(voucher.getArrivalDate());
    }
}
