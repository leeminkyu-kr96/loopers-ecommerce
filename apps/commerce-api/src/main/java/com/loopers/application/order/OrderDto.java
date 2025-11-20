package com.loopers.application.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class OrderDto {

    public record CreateRequest(
        @NotNull Long productId,
        @Min(0) Integer quantity
    ) {}


}