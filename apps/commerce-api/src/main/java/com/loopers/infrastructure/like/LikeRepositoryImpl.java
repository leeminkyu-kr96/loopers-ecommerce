package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    // 좋아요 여부 조회
    @Override
    public Optional<Like> findByUserAndProduct(User user, Product product) {
        return likeJpaRepository.findByUserAndProduct(user, product);
    }

    // 사용자가 좋아요한 상품 목록 조회
    @Override
    public List<Product> findLikedProductsByUser(User user) {
        return likeJpaRepository.findByUser(user).stream()
                .map(Like::getProduct)
                .collect(Collectors.toList());
    }

    // 상품의 좋아요 수 조회
    @Override
    public long countByProductLiked(Product product) {
        return likeJpaRepository.countByProduct(product);
    }

    // 상품의 좋아요 수 일괄 집계
    @Override
    public Map<Long, Long> countByProductIdsLiked(Collection<Long> productIds) {
        return likeJpaRepository.countByProductIds(productIds.stream().collect(Collectors.toSet()));
    }

    // 좋아요 등록
    @Override
    public Like save(Like like) {
        return likeJpaRepository.save(like);
    }

    // 좋아요 삭제
    @Override
    public void delete(Like like) {
        likeJpaRepository.delete(like);
    }
}
