package com.example.examine.repository.SaleRepository;

import com.example.examine.entity.Sale.IherbCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IherbCouponRepository extends JpaRepository<IherbCoupon, Long> {
    void deleteAll(); // 전체 삭제용
}