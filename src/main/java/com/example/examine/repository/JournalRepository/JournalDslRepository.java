package com.example.examine.repository.JournalRepository;

import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.QJournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.QJournalSupplementSideEffect;
import com.example.examine.entity.QJournal;
import com.example.examine.service.EntityService.JournalService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.*;

@RequiredArgsConstructor
@Repository
public class JournalDslRepository {

    private final JPAQueryFactory queryFactory;
    private final QJournal journal = QJournal.journal;
    private static final QJournalSupplementEffect jse = QJournalSupplementEffect.journalSupplementEffect; // JSE
    private static final QJournalSupplementSideEffect jsse = QJournalSupplementSideEffect.journalSupplementSideEffect; // JSSE

    public List<Long> get(
            List<Long> trialDesignIds,
            List<Integer> blinds,
            List<Boolean> parallels,
            List<Long> supplementIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            String keyword,
            JournalService.SortField sort,
            boolean asc,
            int limit,
            int offset
    ) {
        Predicate where = ExpressionUtils.allOf(createFilterCondition(trialDesignIds, blinds, parallels),
                createKeywordCondition(keyword),
                existsSupplements(supplementIds),
                existsEffects(effectIds),
                existsSideEffects(sideEffectIds));

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        orders.add(sort.getOrderSpecifier(asc));
        orders.add(asc ? journal.id.asc() : journal.id.desc()); // tie-breaker

        return queryFactory.select(journal.id)
                .from(journal)
                .where(where)
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .offset(offset)
                .limit(limit + 1)
                .fetch();
    }

    private BooleanExpression createKeywordCondition(String keyword) {
        return (keyword == null || keyword.isBlank())
                ? null
                : journal.title.lower().like("%" + keyword.toLowerCase() + "%");
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

    // --- exists 서브쿼리 ---
    private BooleanExpression existsSupplements(List<Long> supplementIds) {
        if (supplementIds == null || supplementIds.isEmpty()) return null;
        BooleanExpression viaJse = JPAExpressions.selectOne().from(jse)
                .where(jse.journal.eq(journal)
                        .and(jse.id.supplementEffectId.supplementId.in(supplementIds)) // 복합키 예시
                ).exists();
        BooleanExpression viaJsse = JPAExpressions.selectOne().from(jsse)
                .where(jsse.journal.eq(journal)
                        .and(jsse.id.supplementSideEffectId.supplementId.in(supplementIds))
                ).exists();
        return viaJse.or(viaJsse);
    }

    private BooleanExpression existsEffects(List<Long> effectIds) {
        if (effectIds == null || effectIds.isEmpty()) return null;
        return JPAExpressions.selectOne().from(jse)
                .where(jse.journal.eq(journal)
                        .and(jse.id.supplementEffectId.effectTagId.in(effectIds))
                ).exists();
    }

    private BooleanExpression existsSideEffects(List<Long> sideEffectIds) {
        if (sideEffectIds == null || sideEffectIds.isEmpty()) return null;
        return JPAExpressions.selectOne().from(jsse)
                .where(jsse.journal.eq(journal)
                        .and(jsse.id.supplementSideEffectId.sideEffectTagId.in(sideEffectIds))
                ).exists();
    }

    public List<Journal>findByIdsWithTrialDesign(List<Long> ids) {
        List<Journal> journals = queryFactory.selectFrom(journal)
                .leftJoin(journal.trialDesign).fetchJoin()
                .where(journal.id.in(ids))
                .fetch();

        Map<Long,Integer> pos = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) pos.put(ids.get(i), i);

        Journal[] ordered = new Journal[ids.size()];
        for (Journal j : journals) {
            Integer idx = pos.get(j.getId());
            if (idx != null) ordered[idx] = j;
        }

        return Arrays.stream(ordered).filter(Objects::nonNull).toList();
    }

}
