package com.loopers.infrastructure.product;

package com.loopers.infrastructure.product;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.loopers.domain.product.QProductModel.productModel;
import static com.loopers.domain.like.QLikeModel.likeModel;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> findAll(Pageable pageable, String sort) {
        List<Product> content;
        Long totalCount;

        if ("likes_desc".equals(sort)) {
            // 좋아요 순 정렬 (JOIN, GROUP BY 필요)
            content = queryFactory
                    .select(productModel)
                    .from(productModel)
                    .leftJoin(likeModel).on(likeModel.product.eq(productModel.id))
                    .groupBy(productModel.id)
                    .orderBy(likeModel.id.count().desc())
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();

            totalCount = queryFactory
                    .select(productModel.id.countDistinct())
                    .from(productModel)
                    .fetchOne();

        } else {
            // 일반 정렬 (latest, price_asc)
            OrderSpecifier<?> order = createOrderSpecifier(sort);

            content = queryFactory
                    .selectFrom(productModel)
                    .orderBy(order)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();

            totalCount = queryFactory
                    .select(productModel.count())
                    .from(productModel)
                    .fetchOne();
        }

        if (totalCount == null) {
            totalCount = 0L;
        }

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public Page<Product> findByBrandName(String brandName, Pageable pageable, String sort) {
        return productJpaRepository.findByBrandName(brandName, pageable);
    }

    @Override
    public List<Product> findAllById(Set<Long> ids) {
        return productJpaRepository.findAllById(ids);
    }

}
