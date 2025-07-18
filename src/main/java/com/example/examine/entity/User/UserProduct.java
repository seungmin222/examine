package com.example.examine.entity.User;

import com.example.examine.entity.Product;
import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProduct extends EntityTime {

    @EmbeddedId
    private UserProductId id = new UserProductId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    private Product product;

    private int quantity;

    private boolean checked;

}


