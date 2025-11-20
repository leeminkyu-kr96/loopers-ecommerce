package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Arrays;

public enum Gender {
    MALE, 
    FEMALE;

    public static Gender from(String gender) {
        if (gender == null || gender.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 비어있을 수 없습니다.");
        }
        return Arrays.stream(values())
                .filter(g -> g.name().equalsIgnoreCase(gender))
                .findFirst()
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 성별입니다. (MALE/FEMALE)"));
    }
}