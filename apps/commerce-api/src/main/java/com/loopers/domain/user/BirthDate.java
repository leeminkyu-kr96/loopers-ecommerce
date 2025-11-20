package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public record BirthDate(LocalDate birthDate) {

    public BirthDate {
        //생년월일 validation check
        if (birthDate == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 비어있을 수 없습니다.");
        }

        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 미래 날짜일 수 없습니다.");
        }

        LocalDate minDate = today.minusYears(150);
        if (birthDate.isBefore(minDate)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일이 유효한 범위를 벗어났습니다.");
        }
    }
}
