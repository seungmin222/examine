package com.example.examine.repository.JournalRepository;

import com.example.examine.entity.Journal;
import com.example.examine.entity.QJournal;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class JournalQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QJournal journal = QJournal.journal;

    // 📋 전체 목록 정렬 + 페이징
    public List<Journal> findAll(String sort, boolean asc, int limit, int offset) {
        return baseQuery(sort, asc, limit, offset, null);
    }

    public List<Long> findAllIds(String sort, boolean asc, int limit, int offset) {
        return idQuery(sort, asc, limit, offset, null);
    }

    // 🔍 검색 + 정렬 + 페이징
    public List<Journal> findByKeyword(String keyword, String sort, boolean asc, int limit, int offset) {
        BooleanExpression condition = createKeywordCondition(keyword);
        return baseQuery(sort, asc, limit, offset, condition);
    }

    private BooleanExpression createKeywordCondition(String keyword) {
        return (keyword == null || keyword.isBlank())
                ? null
                : journal.title.lower().like("%" + keyword.toLowerCase() + "%");
    }

    //태그 필터링
    public List<Long> filterByTag(List<Long> trialDesignIds, List<Integer> blinds, List<Boolean> parallels, String sort, boolean asc, int limit, int offset) {
        BooleanExpression condition = createFilterCondition(trialDesignIds, blinds, parallels);

        JPQLQuery<Long> query = queryFactory
                .select(journal.id)
                .from(journal)
                .orderBy(JournalSortProperty.fromString(sort).getOrderSpecifier(asc))
                .offset(offset)
                .limit(limit)
                .where(condition);
        return query.fetch();
    }

    private BooleanExpression createFilterCondition(List<Long> trialDesignIds, List<Integer> blinds, List<Boolean> parallels) {
        BooleanExpression condition = null;

        if (trialDesignIds != null && !trialDesignIds.isEmpty()) {
            condition = journal.trialDesign.id.in(trialDesignIds);
        }

        if (blinds != null && !blinds.isEmpty()) {
            BooleanExpression blindExpr = journal.blind.in(blinds);
            condition = (condition == null) ? blindExpr : condition.and(blindExpr);
        }

        if (parallels != null && !parallels.isEmpty()) {
            BooleanExpression parallelExpr = journal.parallel.in(parallels);
            condition = (condition == null) ? parallelExpr : condition.and(parallelExpr);
        }

        return condition;
    }

    // 📦 공통 로직 (조건 있으면 where 추가)
    private List<Journal> baseQuery(String sort, boolean asc, int limit, int offset, @Nullable BooleanExpression condition) {
        JPQLQuery<Journal> query = queryFactory
                .selectFrom(journal)
                .leftJoin(journal.trialDesign).fetchJoin()
                .orderBy(JournalSortProperty.fromString(sort).getOrderSpecifier(asc))
                .offset(offset)
                .limit(limit);

        if (condition != null) {
            query.where(condition);
        }

        return query.fetch();
    }

    private List<Long> idQuery(String sort, boolean asc, int limit, int offset, @Nullable BooleanExpression condition) {
        JPQLQuery<Long> query = queryFactory
                .select(journal.id)
                .from(journal)
                .orderBy(JournalSortProperty.fromString(sort).getOrderSpecifier(asc))
                .offset(offset)
                .limit(limit);

        if (condition != null) {
            query.where(condition);
        }

        return query.fetch();
    }



    public enum JournalSortProperty {
        title(QJournal.journal.title),
        createdAt(QJournal.journal.createdAt);

        private final ComparableExpressionBase<?> expression;

        JournalSortProperty(ComparableExpressionBase<?> expression) {
            this.expression = expression;
        }

        public OrderSpecifier<?> getOrderSpecifier(boolean asc) {
            return asc ? expression.asc() : expression.desc();
        }

        public static JournalSortProperty fromString(String raw) {
            return Arrays.stream(values())
                    .filter(v -> v.name().equalsIgnoreCase(raw))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sort property: " + raw));
        }
    }


}
