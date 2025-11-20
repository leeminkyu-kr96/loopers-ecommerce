package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class LikeService {

    private final LikeRepository likeRepository;

    // 좋아요 등록: 좋아요가 없으면 추가, 있으면 취소
    @Transactional
    public void addLike(User user, Product product) {
        var existing = likeRepository.findByUserAndProduct(user, product);
        if (existing.isEmpty()) {
            Like newLike = new Like(user, product);
            likeRepository.save(newLike);
        }
    }

    // 좋아요 취소: 좋아요가 있으면 취소, 없으면 추가
    @Transactional
    public void removeLike(User user, Product product) {
        var existing = likeRepository.findByUserAndProduct(user, product);
        existing.ifPresent(likeRepository::delete);
    }

    // 좋아요 여부 확인
    @Transactional(readOnly = true)
    public boolean isLiked(User user, Product product) {
        return likeRepository.findByUserAndProduct(user, product).isPresent();
    }

    // 좋아요한 상품 목록 조회
    @Transactional(readOnly = true)
    public List<Product> getLikedProducts(User user) {
        return likeRepository.findLikedProductsByUser(user);
    }

    // 좋아요 수 조회
    @Transactional(readOnly = true)
    public long getLikeCount(Product product) {
        return likeRepository.countByProductLiked(product);
    }

    // 좋아요 수 일괄 집계
    @Transactional(readOnly = true)
    public Map<Long, Long> getLikeCounts(List<Product> products) {
        var ids = products.stream().map(Product::getId).collect(Collectors.toSet());
        return likeRepository.countByProductIdsLiked(ids);
    }
}
