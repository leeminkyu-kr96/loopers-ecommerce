package com.loopers.application.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.util.List;

public class OrderDto {

    record CreateOrderItemRequest(
        @NotNull Long productId,
        @Min(1) Integer quantity
    ) {}

    public record CreateRequest(
        @NotNull List<CreateOrderItemRequest> items
    ) {}
}