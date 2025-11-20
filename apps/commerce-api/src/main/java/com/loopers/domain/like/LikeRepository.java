package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LikeRepository {

    // 좋아요 여부 조회
    Optional<Like> findByUserAndProduct(User user, Product product);

    // 사용자가 좋아요한 상품 목록 조회
    List<Product> findLikedProductsByUser(User user);

    // 상품의 좋아요 수 조회
    long countByProductLiked(Product product);

    // 상품의 좋아요 수 일괄 집계
    Map<Long, Long> countByProductIdsLiked(Collection<Long> productIds);

    // 좋아요 등록
    Like save(Like like);

    // 좋아요 삭제
    void delete(Like like);
}
