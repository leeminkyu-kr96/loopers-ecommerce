package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.common.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

@RequiredArgsConstructor
@Component
public class ProductService {

    private final ProductRepository productRepository;

    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public Page<Product> getProducts(Pageable pageable, String sort, String brandName) {
        if (sort.isBlank()) {
            sort = "lastes";
        }

        Page<Product> productPage;
        if (brandName != null && !brandName.isBlank()) {
            productPage = productRepository.findByBrandName(brandName, pageable, sort);
        } else {
            productPage = productRepository.findAll(pageable, sort);
        }

        return null;
    }

//        List<ProductModel> products = productPage.getContent();
//        Map<Long, Long> likeCounts = likeRepository
//                .countByProductIdsLiked(products.stream().map(ProductModel::getId).collect(Collectors.toSet()));
//        products.forEach(product -> product.setLikeCount(likeCounts.getOrDefault(product.getId(), 0L)));
//
//        // likes_desc 정렬은 메모리에서 처리
//        if ("likes_desc".equals(sort)) {
//            products.sort((a, b) -> Long.compare(
//                    b.getLikeCount() != null ? b.getLikeCount() : 0L,
//                    a.getLikeCount() != null ? a.getLikeCount() : 0L));
//
//            // 정렬된 리스트로 새로운 Page 객체 생성하여 반환
//            return new PageImpl<>(products, pageable, productPage.getTotalElements());
//        }
//
//        return productPage;


    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            product.setLikeCount(likeRepository.countByProductLiked(product));
        }
        return product;
    }

    @Transactional(readOnly = true)
    public Optional<Quantity> getQuantity(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));
        return Optional.of(product.getQuantity());
    }

    @Transactional
    public void updateQuantity(Long id, Quantity quantityToDecrease) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));

        product.decreaseQuantity(quantityToDecrease);
    }
}
