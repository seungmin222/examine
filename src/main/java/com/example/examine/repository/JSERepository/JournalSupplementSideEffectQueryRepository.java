package com.example.examine.repository.JSERepository;

import com.example.examine.entity.JournalSupplementEffect.QJournalSupplementSideEffect;
import com.example.examine.entity.QJournal;
import com.example.examine.repository.JournalRepository.JournalQueryRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class JournalSupplementSideEffectQueryRepository {

    private final JPAQueryFactory query;
    private final QJournalSupplementSideEffect jsse = QJournalSupplementSideEffect.journalSupplementSideEffect;
    private final QJournal journal = QJournal.journal;

    public List<Long> findJournalIdsBySupplementIds(
            List<Long> supplementIds,
            String sort,
            boolean asc,
            int limit,
            int offset
    ) {
        if (supplementIds == null || supplementIds.isEmpty()) return List.of();

        BooleanExpression where = jsse.id.supplementSideEffectId.supplementId.in(supplementIds);
        return baseQuery(where, sort, asc, limit, offset);
    }

    public List<Long> findJournalIdsBySideEffectIds(
            List<Long> sideEffectIds,
            String sort,
            boolean asc,
            int limit,
            int offset
    ) {
        if (sideEffectIds == null || sideEffectIds.isEmpty()) return List.of();

        BooleanExpression where = jsse.id.supplementSideEffectId.sideEffectTagId.in(sideEffectIds);
        return baseQuery(where, sort, asc, limit, offset);
    }

    private List<Long> baseQuery(
            BooleanExpression where,
            String sort,
            boolean asc,
            int limit,
            int offset
    ) {
        return query
                .select(jsse.id.journalId)
                .from(jsse)
                .join(jsse.journal, journal)
                .where(where)
                .groupBy(jsse.id.journalId)
                .orderBy(JournalQueryRepository.JournalSortProperty.fromString(sort).getOrderSpecifier(asc))
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
