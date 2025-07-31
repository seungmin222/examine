package com.example.examine.repository.JSERepository;

import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.JournalSupplementEffect.JSEId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JSERepository<J extends JSE<I>, I extends JSEId> extends JpaRepository<J, I> {
    // 공통 쿼리 정의 가능
}
