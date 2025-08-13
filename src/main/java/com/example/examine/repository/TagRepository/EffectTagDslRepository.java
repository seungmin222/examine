package com.example.examine.repository.TagRepository;

import com.example.examine.entity.Journal;
import com.example.examine.entity.QJournal;
import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Effect.QEffectTag;
import com.example.examine.service.EntityService.TagService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EffectTagDslRepository extends TagDslRepository<EffectTag> {

    private final QEffectTag effect = QEffectTag.effectTag;

    public EffectTagDslRepository(JPAQueryFactory queryFactory) {
        super(queryFactory, EffectTag.class, "effectTag");
    }

    @Override
    protected QEffectTag getRoot() {
        return effect;
    }

    @Override
    protected StringPath getKorNamePath() {
        return effect.korName;
    }

    @Override
    protected StringPath getEngNamePath() {
        return effect.engName;
    }
}
