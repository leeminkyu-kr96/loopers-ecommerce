package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    Page<Product> findAllByDeletedAtIsNull(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.brand.name = :brandName AND p.deletedAt IS NULL")
    Page<Product> findByBrandNameAndDeletedAtIsNull(@Param("brandName") String brandName, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
