package com.epam.finaltask.annotations;

import com.epam.finaltask.utils.VoucherDatesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VoucherDatesValidator.class)
public @interface ValidVoucherDates {
    String message() default "{voucher.dates.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
