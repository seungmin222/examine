package com.example.examine.repository.JSERepository;

import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.QJournalSupplementSideEffect;
import com.example.examine.entity.QJournal;
import com.example.examine.service.EntityService.JournalService;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class JournalSupplementSideEffectDslRepository {

    private final JPAQueryFactory query;
    private final QJournalSupplementSideEffect jsse = QJournalSupplementSideEffect.journalSupplementSideEffect;
    private final QJournal journal = QJournal.journal;

    public List<Long> findJournalIdsBySupplementIds(
            List<Long> supplementIds,
            JournalService.SortField sort,
            boolean asc,
            Journal left,
            Journal right
    ) {
        if (supplementIds == null || supplementIds.isEmpty()) return List.of();

        BooleanExpression where = jsse.id.supplementSideEffectId.supplementId.in(supplementIds);
        return baseQuery(where, sort, asc, left, right);
    }

    public List<Long> findJournalIdsBySideEffectIds(
            List<Long> sideEffectIds,
            JournalService.SortField sort,
            boolean asc,
            Journal left,
            Journal right
    ) {
        if (sideEffectIds == null || sideEffectIds.isEmpty()) return List.of();

        BooleanExpression where = jsse.id.supplementSideEffectId.sideEffectTagId.in(sideEffectIds);
        return baseQuery(where, sort, asc, left, right);
    }

    private List<Long> baseQuery(
            BooleanExpression where,
            JournalService.SortField sort,
            boolean asc,
            Journal left,
            Journal right
    ) {
        return query
                .select(jsse.id.journalId)
                .from(jsse)
                .join(jsse.journal, journal)
                .where(where.and(sort.between(journal, left, right, asc)))
                .groupBy(jsse.id.journalId)
                .orderBy(sort.getOrderSpecifier(asc))
                .fetch();
    }

}
