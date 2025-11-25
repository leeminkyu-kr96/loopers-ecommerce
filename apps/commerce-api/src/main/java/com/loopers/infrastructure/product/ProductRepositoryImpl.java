package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public Page<Product> findByBrandName(String brandName, Pageable pageable) {
        return productJpaRepository.findByBrandNameAndDeletedAtIsNull(brandName, pageable);
    }

    @Override
    public List<Product> findAllById(Set<Long> ids) {
        return productJpaRepository.findAllById(ids);
    }

    @Override
    public Optional<Product> findByIdWithLock(Long id) {
        return productJpaRepository.findByIdWithLock(id);
    }

}
