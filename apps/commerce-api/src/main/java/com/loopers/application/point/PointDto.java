package com.loopers.application.point;

import com.loopers.domain.common.Money;
import jakarta.validation.constraints.NotNull;

public class PointDto {

    public record ChargeRequest(
        @NotNull(message = "충전 금액은 필수입니다.")
        Money amount
    ) {}
    
}