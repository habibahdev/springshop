package com.springshop.springshop.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String name,
        @Size(max = 2000) String description,
        @NotNull @Positive BigDecimal price,
        @Min(0) int stock,
        String imageUrl,
        Long categoryId
        ) {
}
