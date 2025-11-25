package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Page<Product> findAll(Pageable pageable);
    
    Page<Product> findByBrandName(String brandName, Pageable pageable);

    List<Product> findAllById(Set<Long> ids);

    Optional<Product> findByIdWithLock(Long id);

    Optional<Product> findById(Long id);
}