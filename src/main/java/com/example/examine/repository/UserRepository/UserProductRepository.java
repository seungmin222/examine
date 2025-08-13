package com.example.examine.repository.UserRepository;

import com.example.examine.entity.User.UserProduct;
import com.example.examine.entity.User.UserProductId;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface UserProductRepository extends JpaRepository<UserProduct, UserProductId> {

    @Query("""
        select coalesce(sum(p.price * up.quantity), 0)
        from UserProduct up
        join up.product p
        where up.id.userId = :userId
          and up.checked = true
    """)
    BigDecimal sumCheckedTotal(@Param("userId") Long userId);
}
