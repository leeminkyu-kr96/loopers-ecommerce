package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.HashSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
        // 기본 정렬값 설정
        if (sort == null || sort.isBlank()) {
            sort = "latest";
        }

        // 기본 페이지 크기 20개 설정
        int pageSize = pageable.getPageSize();
        if (pageSize <= 0) {
            pageSize = 20;
        }

        // sort 문자열을 Sort 객체로 변환
        Sort sortObj = convertToSort(sort);
        Pageable adjustedPageable = PageRequest.of(pageable.getPageNumber(), pageSize, sortObj);

        // 브랜드명이 있으면 브랜드별 조회, 없으면 전체 조회
        Page<Product> productPage;
        if (brandName == null || brandName.isBlank()) {
            productPage = productRepository.findAll(adjustedPageable);
        } else {
            productPage = productRepository.findByBrandName(brandName, adjustedPageable);
        }

        return productPage;
    }

    private Sort convertToSort(String sort) {
        if ("price_asc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "price.value");
        } else if ("likes_desc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "totalLikeCount");
        } else {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));
        product.setLikeCount(likeRepository.countByProductLiked(product));
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
        Product product = productRepository.findByIdWithLock(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));

        product.decreaseQuantity(quantityToDecrease);
    }

    @Transactional
    public Product getProductWithLockAndDecreaseQuantity(Long id, Quantity quantityToDecrease) {
        Product product = productRepository.findByIdWithLock(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));

        product.decreaseQuantity(quantityToDecrease);
        return product;
    }

    @Transactional(readOnly = true)
    public List<Product> findAllById(List<Long> ids) {
        return productRepository.findAllById(new HashSet<>(ids));
    }
}
