// apps/commerce-api/src/main/java/com/loopers/application/user/UserInfo.java

package com.loopers.application.user;

import com.loopers.domain.user.User;

public record UserInfo(String userId, String email, String birthDate, String gender) {
    public static UserInfo from(User model) {
        return new UserInfo(
            model.getUserId().userId(),
            model.getEmail().email(),
            model.getBirthDate().birthDate().toString(),
            model.getGender().name()
        );
    }
}