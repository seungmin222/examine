package com.example.examine.entity.User;

import com.example.examine.entity.Page;
import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_page")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPage extends EntityTime {

    @EmbeddedId
    private UserPageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pageId")
    @JoinColumn(name = "page_id")
    private Page page;
}

