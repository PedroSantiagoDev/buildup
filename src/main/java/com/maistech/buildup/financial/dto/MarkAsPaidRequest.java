package com.maistech.buildup.financial.dto;

import com.maistech.buildup.financial.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MarkAsPaidRequest(
    @NotNull(message = "Payment date is required") LocalDate paymentDate,

    @NotNull(message = "Payment method is required") PaymentMethod paymentMethod
) {}
