package com.connectify.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record IzipayCheckoutSession(
        String orderId,
        String firstName,
        String lastName,
        String dni,
        String phone,
        String email,
        BigDecimal expectedTotal
) implements Serializable {
}
