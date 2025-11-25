package com.loopers.application.like;

import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final LikeService likeService;
    private final UserService userService;
    private final ProductService productService;

    /**
     * 상품에 좋아요를 추가합니다.
     * 멱등성 보장: 이미 좋아요가 있으면 아무 작업도 하지 않습니다.
     */
    @Transactional
    public void addLike(UserId userId, Long productId) {
        User user = userService.getUser(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));
        
        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }

        likeService.addLike(user, product);
    }

    /**
     * 상품의 좋아요를 취소합니다.
     * 멱등성 보장: 좋아요가 없으면 아무 작업도 하지 않습니다.
     */
    @Transactional
    public void removeLike(UserId userId, Long productId) {
        User user = userService.getUser(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));
        
        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }

        likeService.removeLike(user, product);
    }

    /**
     * 사용자가 좋아요한 상품 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<LikeProductInfo> getLikedProducts(UserId userId) {
        User user = userService.getUser(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));

        List<Product> products = likeService.getLikedProducts(user);
        return products.stream()
                .map(LikeProductInfo::from)
                .collect(Collectors.toList());
    }
}
