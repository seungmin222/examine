import { http, toQuery } from './http';

const BASE = '/api/journals'; // 백엔드에 맞게 조정

// 선택 태그를 쿼리 파라미터로 매핑 (프로젝트에 맞게 key 이름만 수정하면 됨)
function normalizeFilters(filters = {}) {
    const {
        supplement = [],
        effect = [],
        sideEffect = [],
        trialDesign = [],
        blind = [],
        parallel = [],
        ...rest
    } = filters;

    return {
        supplement,
        effect,
        sideEffect,
        trialDesign,
        blind,
        parallel,
        ...rest,
    };
}

export async function getJournals({
                                       offset = 0,
                                       limit = 30,
                                       sort = 'title',
                                       asc = true,
                                       keyword = '',
                                       filters = {},
                                       signal,
                                   } = {}) {
    const query = toQuery({ offset, limit, sort, asc, keyword, ...normalizeFilters(filters) });
    return http(`${BASE}?${query}`, { signal });
}

export async function updateJournal(id, payload, { signal } = {}) {
    if (!id) throw new Error('updateJournal: id가 필요합니다');
    return http(`${BASE}/${id}`, { method: 'PUT', body: payload, signal });
}

export async function deleteJournal(id, { signal } = {}) {
    if (!id) throw new Error('deleteJournal: id가 필요합니다');
    return http(`${BASE}/${id}`, { method: 'DELETE', signal });
}