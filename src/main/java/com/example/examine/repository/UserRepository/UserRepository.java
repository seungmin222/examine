package com.example.examine.repository.UserRepository;

import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.User.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("""
    SELECT DISTINCT s FROM Supplement s
    JOIN FETCH s.effects
    WHERE s.id IN :ids
""")
    List<Supplement> fetchEffectsByIds(@Param("ids") List<Long> ids);

    @Query("""
    SELECT DISTINCT s FROM Supplement s
    JOIN FETCH s.sideEffects
    WHERE s.id IN :ids
""")
    List<Supplement> fetchSideEffectsByIds(@Param("ids") List<Long> ids);

    @Query("""
    SELECT DISTINCT s FROM Supplement s
    LEFT JOIN FETCH s.types
    WHERE s.id IN :ids
""")
    List<Supplement> fetchTypesByIds(@Param("ids") List<Long> ids, Sort sort);
}
