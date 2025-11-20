// ProductJpaRepository.java (수정)
package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM ProductModel p WHERE p.brand.name = :brandName ORDER BY p.likeCount DESC")
    Page<Product> findByBrandName(@Param("brandName") String brandName, Pageable pageable);
}
