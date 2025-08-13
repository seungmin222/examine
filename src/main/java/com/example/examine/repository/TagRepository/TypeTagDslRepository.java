package com.example.examine.repository.TagRepository;

import com.example.examine.entity.Tag.QTypeTag;
import com.example.examine.entity.Tag.TypeTag;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TypeTagDslRepository extends TagDslRepository<TypeTag> {

    private final QTypeTag typeTag = QTypeTag.typeTag;

    public TypeTagDslRepository(JPAQueryFactory queryFactory) {
        super(queryFactory, TypeTag.class, "typeTag");
    }

    @Override
    protected QTypeTag getRoot() {
        return typeTag;
    }

    @Override
    protected StringPath getKorNamePath() {
        return typeTag.korName;
    }

    @Override
    protected StringPath getEngNamePath() {
        return typeTag.engName;
    }
}
