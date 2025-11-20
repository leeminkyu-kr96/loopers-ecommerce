package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Quantity;
import com.loopers.domain.common.Money;
import com.loopers.domain.brand.Brand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    private String name;

    @Embedded
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Embedded
    private Money price;

    @Embedded
    private Quantity quantity;

    private Long totalLikeCount;

    public Product() {
    }

    public Product(String name, Brand brand, Money price, Quantity quantity, Long totalLikeCount) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 이름은 비어있을 수 없습니다.");
        }
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.quantity = quantity;
        this.totalLikeCount = totalLikeCount;
    }

    public void decreaseQuantity(Quantity quantityToDecrease) {
        if (this.quantity.quantity() < quantityToDecrease.quantity()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }

        this.quantity = new Quantity(this.quantity.quantity() - quantityToDecrease.quantity());

    }

    public void setLikeCount(long likeCount) {
        this.totalLikeCount = likeCount;
    }

}
