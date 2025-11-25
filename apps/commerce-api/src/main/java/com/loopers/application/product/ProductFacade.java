package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.common.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final LikeService likeService;

    // 상품 다건 조회
    @Transactional(readOnly = true)
    public Page<ProductInfo> getProducts(Pageable pageable, String sort, String brandName) {
        Page<Product> productPage = productService.getProducts(pageable, sort, brandName);
        
        // 상품 목록의 좋아요 수 조회 및 설정
        List<Product> products = productPage.getContent();
        Map<Long, Long> likeCounts = likeService.getLikeCounts(products);
        
        // 각 상품에 좋아요 수 설정
        products.forEach(product -> {
            Long likeCount = likeCounts.getOrDefault(product.getId(), 0L);
            product.setLikeCount(likeCount);
        });
        
        return productPage.map(ProductInfo::from);
    }

    // 상품 단건 조회
    @Transactional(readOnly = true)
    public ProductInfo getProduct(Long id) {
        Product product = productService.getProduct(id);
        if (product == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다.");
        }
        
        // 좋아요 수 조회 및 설정 (Product + Brand + Like 조합)
        long likeCount = likeService.getLikeCount(product);
        product.setLikeCount(likeCount);
        
        return ProductInfo.from(product);
    }

    // 상품 재고 조회
    @Transactional(readOnly = true)
    public Quantity getQuantity(Long id) {
        return productService.getQuantity(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));
    }

    // 상품 재고 수정
    @Transactional
    public void updateQuantity(Long id, Quantity quantity) {
        productService.updateQuantity(id, quantity);
    }
}