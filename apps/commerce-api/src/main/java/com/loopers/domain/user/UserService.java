package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

     @Transactional(readOnly = true)
     public User getUser(UserId userId) {
         return userRepository.findByUserId(userId).orElse(null);
     }

    @Transactional
    public User signUp(User userModel) {
        Optional<User> user = userRepository.findByUserId(userModel.getUserId());

        if (user.isPresent()) {
            throw new CoreException(ErrorType.CONFLICT, "[userId = " + userModel.getUserId().userId() + "] 아이디가 중복되었습니다.");
        }
        return userRepository.save(userModel);
    }
}
