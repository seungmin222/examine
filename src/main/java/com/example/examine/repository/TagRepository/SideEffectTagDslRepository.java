package com.example.examine.repository.TagRepository;

import com.example.examine.entity.Tag.Effect.QSideEffectTag;
import com.example.examine.entity.Tag.Effect.SideEffectTag;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class SideEffectTagDslRepository extends TagDslRepository<SideEffectTag> {

    private final QSideEffectTag sideEffect = QSideEffectTag.sideEffectTag;

    public SideEffectTagDslRepository(JPAQueryFactory queryFactory) {
        super(queryFactory, SideEffectTag.class, "sideEffectTag");
    }

    @Override
    protected QSideEffectTag getRoot() {
        return sideEffect;
    }

    @Override
    protected StringPath getKorNamePath() {
        return sideEffect.korName;
    }

    @Override
    protected StringPath getEngNamePath() {
        return sideEffect.engName;
    }
}
