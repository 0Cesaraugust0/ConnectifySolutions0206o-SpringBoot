package com.connectify.dto;

import java.math.BigDecimal;

public record IzipayPaymentRequest(
        String orderId,
        BigDecimal amount,
        String currency,
        String firstName,
        String lastName,
        String dni,
        String phone,
        String email
) {
}
