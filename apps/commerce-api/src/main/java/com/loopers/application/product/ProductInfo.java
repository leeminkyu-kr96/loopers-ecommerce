package com.loopers.application.product;

import com.loopers.domain.common.Money;
import com.loopers.domain.product.Product;

public record ProductInfo(Long id, String name, String brandName, Money price, Long likeCount) {
    public static ProductInfo from(Product model) {
        return new ProductInfo(
            model.getId(),
            model.getName(),
            model.getBrand().getName(),
            model.getPrice(),
            model.getTotalLikeCount()
        );
    }
}