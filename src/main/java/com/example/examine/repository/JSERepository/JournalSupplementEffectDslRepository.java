package com.example.examine.repository.JSERepository;

import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.QJournalSupplementEffect;
import com.example.examine.entity.QJournal;
import com.example.examine.service.EntityService.JournalService;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class JournalSupplementEffectDslRepository {

    private final JPAQueryFactory query;
    private final QJournalSupplementEffect jse = QJournalSupplementEffect.journalSupplementEffect;
    private final QJournal journal = QJournal.journal;

    public List<Long> findJournalIdsBySupplementIds(
            List<Long> supplementIds,
            JournalService.SortField sort,
            boolean asc,
            Journal left,
            Journal right
    ) {
        if (supplementIds == null || supplementIds.isEmpty()) return List.of();

        BooleanExpression where = jse.id.supplementEffectId.supplementId.in(supplementIds);
        return baseQuery(where, sort, asc, left, right);
    }

    public List<Long> findJournalIdsByEffectIds(
            List<Long> effectIds,
            JournalService.SortField sort,
            boolean asc,
            Journal left,
            Journal right
    ) {
        if (effectIds == null || effectIds.isEmpty()) return List.of();

        BooleanExpression where = jse.id.supplementEffectId.effectTagId.in(effectIds);
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
                .select(jse.id.journalId)
                .from(jse)
                .join(jse.journal, journal)
                .where(where.and(sort.between(journal, left, right, asc)))
                .groupBy(jse.id.journalId)
                .orderBy(sort.getOrderSpecifier(asc))
                .fetch();
    }

}
