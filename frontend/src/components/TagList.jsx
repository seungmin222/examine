import SearchForm from "./SearchForm.jsx";
import {useCallback, useEffect, useRef, useState} from "react";
import {getTags} from "@/functions/api/tag.js";
import Tooltip from "@/components/Tooltip.jsx";
import {getTagSuggests} from "@/functions/api/search.js";

export default function TagList( { types, selected, addFilter }) {
    const [tags, setTags] = useState(Object.fromEntries(types.map(type => [type, []])));
    const loadMode = useRef('replace');

    const [tagState, setTagState] = useState({
        query: {
            offset: 0,
            limit: 2,
            sort: 'korName',
            asc: true,
            keyword: '',
            type: types,
        },
        ui: {
            hasMore: true,
            fold: false,
            loading: false
        },
    });

    const loadTags = useCallback(async (mode = 'replace') => {
        if (tagState.ui.loading) return;
        if (mode === 'append' && !tagState.ui.hasMore) return;

        const { type, offset, limit, sort, asc, keyword } = tagState.query;
        setTagState(s => ({ ...s, ui: { ...s.ui, loading: true } }));

        try {
            const res = await getTags({ type, offset, limit, sort, asc, keyword });

            const newMap = res?.data?.map ?? res?.map ?? {};

            setTags(prev => {
                const updated = { ...prev };
                for (const [t, tagList] of Object.entries(newMap)) {
                    const withType = (tagList ?? []).map(tag => ({ ...tag, type: t }));
                    updated[t] =
                        mode === 'replace'
                            ? withType
                            : [ ...(updated[t] || []), ...withType ];
                }
                return updated;
            });

            // 서버 값 우선, 없으면 한 타입이라도 페이지가 꽉 찼는지만 확인
            const pageLimit = Number(limit);
            const hasMore =
                res?.hasMore ??
                Object.values(newMap).some(list => (list?.length || 0) === pageLimit);

            // ✅ offset은 received 없이 limit 기준으로 통일
            const nextOffset = (mode === 'replace' ? 0 : offset) + pageLimit;

            setTagState(prev => ({
                ...prev,
                query: { ...prev.query, offset: nextOffset },
                ui: { ...prev.ui, hasMore, loading: false },
            }));
        } catch (err) {
            console.error('[loadTags] error', err);
            setTagState(s => ({ ...s, ui: { ...s.ui, loading: false } }));
        }
    }, [tagState.query, tagState.ui.loading, tagState.ui.hasMore, setTagState, setTags]);




    useEffect(() => {
        const mode = loadMode.current ?? 'replace';
        loadTags(mode).finally(() => {
            loadMode.current = 'replace';
        });
    }, [
        tagState.query.keyword,
        tagState.query.sort,
        tagState.query.asc,
        tagState.query.limit,
    ]);

    const tagSearch = (kw) => {
        setTagState(prev => ({
            ...prev,
            query: { ...prev.query, keyword: kw, offset: 0 }, // 검색은 보통 offset 초기화
        }));
    };

    const tagSuggestion = (keyword) => {
        return getTagSuggests({type: types, keyword});
    }

    const handleClick = (type) => (e) => {
        const li = e.target.closest("li[data-id]");
        if (!li) return;
        const id = Number(li.dataset.id)
        if (!type) return;
        addFilter(type, id);
    };

    const changeLimit = (e) => {
        loadMode.current = 'append';
        setTagState(prev => ({
            ...prev,
            query: { ...prev.query, limit: Number(e.target.value) },
        }));
    };

    const toggleAsc = () => {
        setTagState(prev => ({
            ...prev,
            query: { ...prev.query, asc: !prev.query.asc },
        }));
    };

    return (<>
        <div className='flex flex-col items-center justify-center'>
            <div className="flex gap-2 items-center justify-center">
                <h2>태그 리스트
                <Tooltip
                    trigger={<span className="note">[1]</span>}
                    content={<p className='whitespace-nowrap'>태그를 토글해 테이블을 필터링 할 수 있습니다</p>}
                    placement='top'
                    tail='true'
                />
                </h2>
                <SearchForm
                    id="tag-search-form"
                    placeholder="태그 검색"
                    inputClass='w-32'
                    submitFn={tagSearch}
                    changeFn={tagSuggestion}
                />
                <button id="tag-toggle-fold" onClick={() => setTagState(prev => ({ ...prev, fold: !prev.fold }))}>
                    {tagState.fold ? '펼치기' : '접기'}
                </button>

                <select
                    name="typeId"
                    id="tag-sort"
                    value={tagState.sort}
                    onChange={(e) => setTagState(prev => ({ ...prev, sort: e.target.value }))}
                >
                    <option value="korName">한글순</option>
                    <option value="engName">영어순</option>
                </select>
                <button onClick={toggleAsc}>{tagState.query.asc ? '정방향' : '역방향'}</button>
            </div>
        <div className='flex w-full justify-between gap-8'>
        {tagState.query.type.map(type => (
            <div
                key={type}
                id={`${type}-list`}
                onClick={handleClick(type)}
            >
                <h2 className="text-center">
                    {type === 'supplement' && '성분'}
                    {type === 'effect' && '기대효과'}
                    {type === 'sideEffect' && '부작용'}
                </h2>
                {tags[type].map(tag => {
                    const isSelected = (selected[type] ?? []).includes(tag.id);
                    return (
                        <li
                            key={tag.id}
                            data-id={tag.id}
                            className={isSelected ? 'selected' : ''}
                        >
                            {tag.korName}
                        </li>
                    );
                })}
            </div>
        ))}
        </div>
        <div className={`${tagState.ui.hasMore ? '' : 'hidden'} flex mt-4`}>
            <button
                onClick={() => loadTags('append')}
            >
                더 보기
            </button>
            <select
                className='w-24'
                onChange={(e)=>changeLimit(e)}
            >
                <option value='10'>10개씩</option>
                <option value='50'>50개씩</option>
                <option value='100'>100개씩</option>
            </select>
        </div>
        </div>

    </>);
};
