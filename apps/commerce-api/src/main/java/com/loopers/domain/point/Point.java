package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "point")
public class Point extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "balance"))
    private Money balance;

    protected Point() {}

    private Point(User user, Money balance) {
        this.user = user;
        this.balance = balance;
    }

    public static Point create(User user) {
        return new Point(user, new Money(0));
    }

    public void charge(Money amount) {
        if (amount.value() <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 0원보다 커야 합니다.");
        }
        long newBalance = this.balance.value() + amount.value();
        this.balance = new Money(newBalance);
    }

    public void use(Money amount) {
        if (this.balance.value() < amount.value()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        long newBalance = this.balance.value() - amount.value();
        this.balance = new Money(newBalance);
    }
}