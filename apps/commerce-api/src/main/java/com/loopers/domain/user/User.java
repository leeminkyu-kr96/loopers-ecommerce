package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Embedded;
import lombok.Getter;

@Getter
@Entity
@Table(name = "user")
public class User extends BaseEntity {

    @Embedded
    private UserId userId;

    @Embedded
    private Email email;

    @Embedded
    private Gender gender;

    @Embedded
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
