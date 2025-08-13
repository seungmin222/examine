package com.example.examine.repository.TagRepository;

import com.example.examine.entity.Tag.Brand;
import com.example.examine.entity.Tag.QBrand;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.stereotype.Repository;

@Repository
public class BrandDslRepository extends TagDslRepository<Brand> {

    private final QBrand brand = QBrand.brand;

    public BrandDslRepository(JPAQueryFactory queryFactory) {
        super(queryFactory, Brand.class, "brand");
    }

    @Override
    protected QBrand getRoot() {
        return brand;
    }

    @Override
    protected StringPath getKorNamePath() {
        return brand.korName;
    }

    @Override
    protected StringPath getEngNamePath() {
        return brand.engName;
    }

}
