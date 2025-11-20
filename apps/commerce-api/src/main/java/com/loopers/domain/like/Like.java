package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.domain.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "like",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})
)
public class Like extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Like() {
    }

    public Like(User user, Product product) {
        this.user = user;
        this.product = product;
    }

}

