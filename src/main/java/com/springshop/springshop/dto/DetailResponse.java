package com.springshop.springshop.dto;

import java.math.BigDecimal;

public record DetailResponse(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
