import { useCallback, useEffect, useMemo, useReducer, useRef } from 'react';
import * as api from '../api/journals';

const initialQuery = {
    offset: 0,
    limit: 30,
    sort: 'title',
    direction: 'asc',
    keyword: '',
    filters: { supplement: [], effect: [], sideEffect: [], trialDesign: [], blind: [], parallel: [] },
};

function reducer(state, action) {
    switch (action.type) {
        case 'replace':
            return { ...state, list: action.list, hasMore: action.hasMore, query: { ...state.query, offset: action.nextOffset } };
        case 'append':
            return {
                ...state,
                list: [...state.list, ...action.list],
                hasMore: action.hasMore,
                query: { ...state.query, offset: action.nextOffset },
            };
        case 'setSort':
            return { ...state, query: { ...state.query, sort: action.sort, direction: action.direction, offset: 0 } };
        case 'setKeyword':
            return { ...state, query: { ...state.query, keyword: action.keyword, offset: 0 } };
        case 'setFilters':
            return { ...state, query: { ...state.query, filters: action.filters, offset: 0 } };
        case 'setLimit':
            return { ...state, query: { ...state.query, limit: action.limit, offset: 0 } };
        case 'loading':
            return { ...state, loading: action.loading };
        case 'error':
            return { ...state, error: action.error, loading: false };
        default:
            return state;
    }
}

export default function useJournals(opts = {}) {
    const init = useMemo(() => ({ list: [], hasMore: true, loading: false, error: null, query: { ...initialQuery, ...opts } }), [opts]);
    const [state, dispatch] = useReducer(reducer, init);
    const abortRef = useRef(null);

    const load = useCallback(async (mode = 'replace') => {
        if (abortRef.current) abortRef.current.abort();
        const ctrl = new AbortController();
        abortRef.current = ctrl;

        dispatch({ type: 'loading', loading: true });
        try {
            const { offset, limit, sort, direction, keyword, filters } = state.query;
            const data = await api.listJournals({ offset, limit, sort, direction, keyword, filters, signal: ctrl.signal });

            // 백엔드 응답 형태에 따라 조정: 배열만 오면 길이로 hasMore 추정
            const items = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : [];
            const hasMore = items.length === limit; // 또는 data.total / next 존재 여부로 계산
            const nextOffset = offset + items.length;

            dispatch({ type: mode, list: items, hasMore, nextOffset });
            dispatch({ type: 'loading', loading: false });
            return items;
        } catch (e) {
            if (e.name === 'AbortError') return [];
            dispatch({ type: 'error', error: e });
            return [];
        }
    }, [state.query]);

    const reload = useCallback(() => load('replace'), [load]);
    const loadMore = useCallback(() => load('append'), [load]);

    const setSort = useCallback((sort, direction) => dispatch({ type: 'setSort', sort, direction }), []);
    const setKeyword = useCallback((keyword) => dispatch({ type: 'setKeyword', keyword }), []);
    const setFilters = useCallback((filters) => dispatch({ type: 'setFilters', filters }), []);
    const setLimit = useCallback((limit) => dispatch({ type: 'setLimit', limit }), []);

    // 초기 및 sort/keyword/filters 변경 시 재로딩
    useEffect(() => { reload(); }, [state.query.sort, state.query.direction, state.query.keyword, state.query.filters, state.query.limit]);

    // CRUD 도우미
    const remove = useCallback(async (id) => {
        await api.deleteJournal(id);
        // 간단히 클라이언트에서 제거
        const next = state.list.filter((it) => it.id !== id);
        const hasMore = next.length >= state.query.limit ? state.hasMore : state.hasMore; // 유지
        dispatch({ type: 'replace', list: next, hasMore, nextOffset: Math.max(0, next.length) });
    }, [state.list, state.query.limit, state.hasMore]);

    const save = useCallback(async (id, payload) => {
        await api.updateJournal(id, payload);
        // 보수적으로 전체 새로고침이 안전
        await reload();
    }, [reload]);

    return {
        list: state.list,
        loading: state.loading,
        error: state.error,
        hasMore: state.hasMore,
        query: state.query,
        setSort, setKeyword, setFilters, setLimit,
        reload, loadMore,
        remove, save,
    };
}