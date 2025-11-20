package com.loopers.application.like;

import com.loopers.application.product.ProductInfo; // ProductInfo import
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.loopers.domain.like.LikeService;
import com.loopers.domain.user.UserService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class LikeFacade {

   private final LikeService likeService;
   private final UserService userService;
   private final ProductService productService;

   // ... (toggleLike, addLike, removeLike, isLiked 메서드는 그대로 유지)

   @Transactional
   public void toggleLike(UserId userId, Long productId) {
      User user = userService.getUser(userId)
              .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));
      if (user == null) {
         throw new CoreException(ErrorType.NOT_FOUND, "유저를 찾을 수 없습니다.");
      }
      Product product = productService.getProduct(productId);
      if (product == null) {
         throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
      }
      likeService.toggleLike(user, product);
   }

   @Transactional
   public void addLike(UserId userId, Long productId) {
      User user = userService.getUser(userId)
              .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));
      if (user == null) {
         throw new CoreException(ErrorType.NOT_FOUND, "유저를 찾을 수 없습니다.");
      }
      Product product = productService.getProduct(productId);
      if (product == null) {
         throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
      }
      likeService.addLike(user, product);
   }

   @Transactional
   public void removeLike(UserId userId, Long productId) {
      User user = userService.getUser(userId)
              .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));
      if (user == null) {
         throw new CoreException(ErrorType.NOT_FOUND, "유저를 찾을 수 없습니다.");
      }
      Product product = productService.getProduct(productId);
      if (product == null) {
         throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
      }
      likeService.removeLike(user, product);
   }

   @Transactional(readOnly = true)
   public boolean isLiked(UserId userId, Long productId) {
      User user = userService.getUser(userId)
              .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));
      Product product = productService.getProduct(productId);
      if (product == null) {
         throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
      }
      return likeService.isLiked(user, product);
   }

   // 🚨 수정됨: List<Product> -> List<ProductInfo>
   @Transactional(readOnly = true)
   public List<ProductInfo> getLikedProducts(UserId userId) {
      User user = userService.getUser(userId)
              .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));
      
      List<Product> products = likeService.getLikedProducts(user);
      
      return products.stream()
            .map(ProductInfo::from)
            .collect(Collectors.toList());
   }  

   @Transactional(readOnly = true)
   public long getLikeCount(Long productId) {
      Product product = productService.getProduct(productId);
      if (product == null) {
          throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
      }
      return likeService.getLikeCount(product);
   }

   @Transactional(readOnly = true)
   public Map<Long, Long> getLikeCounts(List<Long> productIds) {
       List<Product> products = productService.findAllById(productIds);

       return likeService.getLikeCounts(products);
   }
}