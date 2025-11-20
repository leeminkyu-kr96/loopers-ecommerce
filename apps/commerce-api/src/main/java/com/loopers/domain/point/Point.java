package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Embedded;
import lombok.Getter;


@Getter
@Entity
@Table(name = "point")
public class Point extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_model_id")
    private User user;
    @Embedded
    private Money point;

    public Point() {
    }

    public Point(User user, Money point) {

        this.user = user;
        this.point = point;
    }

    public void charge(Money chargePoint) {
        long newPointValue = this.point.value() + chargePoint.value();
        this.point = new Money(newPointValue);
    }

    public void use(Money usePoint) {
        if (this.point.value() < usePoint.value()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }

        long newPointValue = this.point.value() - usePoint.value();
        this.point = new Money(newPointValue);

    }
}
