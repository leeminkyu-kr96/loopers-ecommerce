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

    @Transactional
    public void addLike(User user, Product product) {
        if (likeRepository.findByUserAndProduct(user, product).isPresent()) {
            return; 
        }
        
        Like newLike = Like.create(user, product);
        likeRepository.save(newLike);
    }

    @Transactional
    public void removeLike(User user, Product product) {
        likeRepository.findByUserAndProduct(user, product)
                .ifPresent(likeRepository::delete);
    }

    @Transactional
    public void toggleLike(User user, Product product) {
        likeRepository.findByUserAndProduct(user, product)
            .ifPresentOrElse(
                likeRepository::delete,
                () -> addLike(user, product)
            );
    }

    @Transactional(readOnly = true)
    public boolean isLiked(User user, Product product) {
        return likeRepository.findByUserAndProduct(user, product).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Product> getLikedProducts(User user) {
        return likeRepository.findLikedProductsByUser(user);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Product product) {
        return likeRepository.countByProductLiked(product);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getLikeCounts(List<Product> products) {
        var ids = products.stream().map(Product::getId).collect(Collectors.toSet());
        return likeRepository.countByProductIdsLiked(ids);
    }
}