package com.springshop.springshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DetailRequest(
        @NotNull Long productId,
        @Min(1) int quantity
) {
}
