// apps/commerce-api/src/main/java/com/loopers/domain/user/User.java

package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Embedded
    @AttributeOverride(name = "userId", column = @Column(name = "user_id", nullable = false, unique = true, length = 10))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "email", column = @Column(name = "email", nullable = false))
    private Email email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Embedded
    @AttributeOverride(name = "birthDate", column = @Column(name = "birth_date", nullable = false))
    private BirthDate birthDate;

    protected User() {
    }

    public User(UserId userId, Email email, Gender gender, BirthDate birthDate) {
        this.userId = userId;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
    }
}