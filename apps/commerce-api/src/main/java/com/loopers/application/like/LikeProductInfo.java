package com.loopers.application.like;

import com.loopers.domain.product.Product;

public record LikeProductInfo(Long id, String name, String brandName) {
    public static LikeProductInfo from(Product product) {
        return new LikeProductInfo(
            product.getId(),
            product.getName(),
            product.getBrand().getName()
        );
    }
}
