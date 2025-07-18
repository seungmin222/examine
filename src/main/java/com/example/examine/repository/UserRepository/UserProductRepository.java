package com.example.examine.repository.UserRepository;

import com.example.examine.entity.User.UserProduct;
import com.example.examine.entity.User.UserProductId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProductRepository extends JpaRepository<UserProduct, UserProductId> {

    // ✅ 유저가 장바구니에 담은 전체 상품
    List<UserProduct> findByUserId(Long userId);

    // ✅ 유저의 특정 상품 한 개 (optional로 반환 가능)
    UserProduct findByUserIdAndProductId(Long userId, Long productId);

    // ✅ 상품 기준으로 담은 유저들 (관리용)
    List<UserProduct> findByProductId(Long productId);
}
