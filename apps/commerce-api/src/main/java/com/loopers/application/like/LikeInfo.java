package com.loopers.application.like;

import com.loopers.domain.like.Like;

public record LikeInfo(Long id, Long userId, Long productId) {
    public static LikeInfo from(Like model) {
        return new LikeInfo(
            model.getId(),
            model.getUser().getId(),
            model.getProduct().getId()
        );
    }
}
