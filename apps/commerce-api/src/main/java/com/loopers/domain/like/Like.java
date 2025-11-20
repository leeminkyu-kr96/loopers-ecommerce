package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})
)
public class Like extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected Like() {}

    private Like(User user, Product product) {
        this.user = user;
        this.product = product;
    }

    public static Like create(User user, Product product) {
        if (user == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 정보는 필수입니다.");
        }
        if (product == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 정보는 필수입니다.");
        }
        return new Like(user, product);
    }
}