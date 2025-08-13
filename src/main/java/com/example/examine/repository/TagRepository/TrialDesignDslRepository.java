package com.example.examine.repository.TagRepository;

import com.example.examine.entity.Tag.QTrialDesign;
import com.example.examine.entity.Tag.TrialDesign;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TrialDesignDslRepository extends TagDslRepository<TrialDesign> {

    private final QTrialDesign trialDesign = QTrialDesign.trialDesign;

    public TrialDesignDslRepository(JPAQueryFactory queryFactory) {
        super(queryFactory, TrialDesign.class, "trialDesignTag");
    }

    @Override
    protected QTrialDesign getRoot() {
        return trialDesign;
    }

    @Override
    protected StringPath getKorNamePath() {
        return trialDesign.korName;
    }

    @Override
    protected StringPath getEngNamePath() {
        return trialDesign.engName;
    }
}
