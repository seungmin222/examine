package com.example.examine.repository.TagRepository;

import com.example.examine.entity.Tag.QSupplement;
import com.example.examine.entity.Tag.Supplement;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class SupplementDslRepository extends TagDslRepository<Supplement> {

    private final QSupplement supplement = QSupplement.supplement;

    public SupplementDslRepository(JPAQueryFactory queryFactory) {
        super(queryFactory, Supplement.class, "supplement");
    }

    @Override
    protected QSupplement getRoot() {
        return supplement;
    }

    @Override
    protected StringPath getKorNamePath() {
        return supplement.korName;
    }

    @Override
    protected StringPath getEngNamePath() {
        return supplement.engName;
    }
}
