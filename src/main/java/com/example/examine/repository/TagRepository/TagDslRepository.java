package com.example.examine.repository.TagRepository;

import com.example.examine.entity.Tag.Tag;
import com.example.examine.service.EntityService.TagService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@RequiredArgsConstructor
public abstract class TagDslRepository<T extends Tag> {

    protected final JPAQueryFactory queryFactory;
    private final Class<T> typeClass;
    private final String alias;
    protected abstract StringPath getKorNamePath();
    protected abstract StringPath getEngNamePath();


    public List<T> get(String keyword, TagService.SortField sort, Boolean asc, int limit, int offset) {
        PathBuilder<T> path = new PathBuilder<>(typeClass, alias);
        OrderSpecifier<?> order = sort.getOrderSpecifier(path, asc);

       return queryFactory
                .selectFrom(getRoot())
                .orderBy(order)
                .offset(offset)
                .limit(limit)
                .where(createKeywordCondition(keyword))
                .fetch();
    }

    @Nullable
    private BooleanExpression createKeywordCondition(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        String lowerKeyword = "%" + keyword.toLowerCase() + "%";

        return getKorNamePath().lower().like(lowerKeyword)
                .or(getEngNamePath().lower().like(lowerKeyword));
    }

    // 각 하위 클래스가 자신의 Q타입 리턴
    protected abstract EntityPathBase<T> getRoot();
}
