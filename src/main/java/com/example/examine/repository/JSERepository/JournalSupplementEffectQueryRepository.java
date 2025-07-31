package com.example.examine.repository.JSERepository;

import com.example.examine.entity.JournalSupplementEffect.QJournalSupplementEffect;
import com.example.examine.entity.QJournal;
import com.example.examine.repository.JournalRepository.JournalQueryRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class JournalSupplementEffectQueryRepository {

    private final JPAQueryFactory query;
    private final QJournalSupplementEffect jse = QJournalSupplementEffect.journalSupplementEffect;
    private final QJournal journal = QJournal.journal;

    public List<Long> findJournalIdsBySupplementIds(
            List<Long> supplementIds,
            String sort,
            boolean asc,
            int limit,
            int offset
    ) {
        if (supplementIds == null || supplementIds.isEmpty()) return List.of();

        BooleanExpression where = jse.id.supplementEffectId.supplementId.in(supplementIds);
        return baseQuery(where, sort, asc, limit, offset);
    }

    public List<Long> findJournalIdsByEffectIds(
            List<Long> effectIds,
            String sort,
            boolean asc,
            int limit,
            int offset
    ) {
        if (effectIds == null || effectIds.isEmpty()) return List.of();

        BooleanExpression where = jse.id.supplementEffectId.effectTagId.in(effectIds);
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
                .select(jse.id.journalId)
                .from(jse)
                .join(jse.journal, journal)
                .where(where)
                .groupBy(jse.id.journalId)
                .orderBy(JournalQueryRepository.JournalSortProperty.fromString(sort).getOrderSpecifier(asc))
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
