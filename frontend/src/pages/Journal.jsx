import {useEffect, useState, useRef, useCallback} from 'react';
import TagList from '../components/TagList';
import {
    ArrayCheckboxesByName,
    setupFoldToggle,
    setupSortTrigger,
    setupToggleButton,
    setupModalOpenClose,
    setupSearchForm,
    selectList,
    onlyOneCheckboxes,
    selectChange,
    renderJournals,
    renderTags,
    renderModal,
    loadButton,
    journalEvent,
    createEffectCache,
    createNewAlarm,
    createParam,
    receiveData,
    insertData,
    showLoadBtn
} from '../functions/index';

import { useMemo } from 'react';
import {deleteJournal, getJournals, updateJournal} from "@/functions/api/journal.js";
import SearchForm from "@/components/SearchForm.jsx";
import {createAlarm} from "@/components/AlarmContainer.jsx";
import Tooltip from "@/components/Tooltip.jsx";
import {getJournalSuggests} from "@/functions/api/search.js";

export default function Journal() {
    const [journals, setJournals] = useState([]);
    const tagTypes = ['supplement', 'effect', 'sideEffect'];
    const loadMode = useRef('replace');

    const journalIndexMap = useMemo(() => {
        const map = new Map();
        journals.forEach((j, i) => map.set(j.id, i));
        return map;
    }, [journals]);

    // 📘 Journal
    const [journalState, setJournalState] = useState({
        query: {
            offset: 0,
            limit: 2,
            sort: 'title',
            asc: true,
            keyword: '',
            filters: {
                supplement: [],
                effect: [],
                sideEffect: [],
                trialDesign: [],
                blind: [],
                parallel: [],
            },
        },
        ui: {
            hasMore: true,
            fold: false,
            loading: false,
            editMode: false,
        },
    });


// 📕 Modal
    const [modalState, setModalState] = useState({
        query: {
            offset: 0,
            limit: 2,
            sort: 'korName',
            asc: true,
            keyword: '',
            type: ['supplement', 'effect', 'sideEffect'],
        },
        ui: {
            hasMore: true,
            fold: false,
            loading: false
        },
    });


    useEffect(() => {
        const mode = loadMode.current ?? 'replace';
        loadJournals(mode).finally(() => {
            loadMode.current = 'replace';
        });
    }, [
        journalState.query.keyword,
        journalState.query.sort,
        journalState.query.asc,
        journalState.query.filters,
        journalState.query.limit,
    ]);


    const loadJournals = useCallback(async (mode = 'replace') => {
        // 가드
        if (journalState.ui.loading) return;
        if (mode === 'append' && !journalState.ui.hasMore) return;

        const { limit, sort, asc, keyword, filters } = journalState.query;
        const requestOffset = mode === 'replace' ? 0 : journalState.query.offset;

        // 🔎 디버그
        const stamp = new Date().toISOString();
        console.groupCollapsed(`[loadJournals] ${stamp} mode=${mode}`);
        console.table([{ offset: requestOffset, limit, sort, asc, keyword,
            hasMore: journalState.ui.hasMore, loading: journalState.ui.loading }]);
        console.log('filters:', JSON.parse(JSON.stringify(filters)));
        console.groupEnd();

        // 로딩 시작 + replace면 offset 선반영
        setJournalState(s => ({
            ...s,
            query: { ...s.query, offset: mode === 'replace' ? 0 : s.query.offset },
            ui: { ...s.ui, loading: true },
        }));

        try {
            const res = await getJournals({
                offset: requestOffset, limit, sort, asc, keyword, filters
            });

            const items =
                res?.data?.list ??
                res?.list ??
                res?.content ??
                (Array.isArray(res) ? res : []);

            // replace / append 분기
            setJournals(prev => (mode === 'replace' ? items : [...prev, ...items]));

            // hasMore: 서버 우선, 없으면 길이로 추정
            const hasMore = res?.hasMore ?? (items.length === Number(limit));

            const nextOffset = requestOffset + Number(limit);

            console.groupCollapsed(`[loadJournals] done ${stamp}`);
            console.table([{ received: items.length, nextOffset, hasMore }]);
            console.groupEnd();

            setJournalState(s => ({
                ...s,
                query: { ...s.query, offset: nextOffset },
                ui: { ...s.ui, loading: false, hasMore },
            }));
        } catch (err) {
            console.error('[loadJournals] error', err);
            setJournalState(s => ({ ...s, ui: { ...s.ui, loading: false } }));
        }

        createAlarm('논문을 불러왔습니다.');
    }, [journalState.query, journalState.ui.loading, journalState.ui.hasMore, setJournalState, setJournals]);


    const toggleEdit = () => {
        setJournalState(prev => ({
            ...prev,
            ui: { ...prev.ui, editMode: !prev.ui.editMode },
        }));
    };

    const toggleFold = () => {
        setJournalState(prev => ({
            ...prev,
            ui: { ...prev.ui, fold: !prev.ui.fold },
        }));
    };

    const toggleAsc = () => {
        setJournalState(prev => ({
            ...prev,
            query: { ...prev.query, asc: !prev.query.asc },
        }));
    };


    const changeLimit = (e) => {
        loadMode.current = 'append';
        setJournalState(prev => ({
            ...prev,
            query: { ...prev.query, limit: Number(e.target.value) },
        }));
    };

    const changeSort = (e) => {
        setJournalState(prev => ({
            ...prev,
            query: { ...prev.query, sort: e.target.value },
        }));
    };
    
    const visibleJournals = journalState.fold ? journals.slice(0, 5) : journals;
    const rowRefs = useRef([]);

    const handleDelete = async (id, title) => {
        if (!confirm(`'${title}' 논문을 삭제할까요?`)) return;
        await deleteJournal(id);
        await loadJournals();
    };

    const handleSave = async (journal, rowRef) => {
        const row = rowRef.current;
        const effects = [...row.querySelectorAll('.effect-cash')].map(row => extractEffect(row));
        const sideEffects = [...row.querySelectorAll('.sideEffect-cash')].map(row => extractEffect(row));

        const updated = {
            link: journal.link,
            trialDesignId: parseInt(row.querySelector('[name="trialDesign"]').value),
            blind: parseInt(row.querySelector('[name="blind"]').value),
            parallel: row.querySelector('[name="parallel"]').value,
            durationValue: parseInt(row.querySelector('[name="durationValue"]').value),
            durationUnit: row.querySelector('[name="durationUnit"]').value,
            participants: parseInt(row.querySelector('[name="participants"]').value),
            effects,
            sideEffects
        };
        const res = await updateJournal(journal.id, updated);

        if (res.ok) {
            createNewAlarm("논문이 수정되었습니다.");
            await loadJournals();
        } else {
            alert("수정 실패");
        }
    };

    const extractEffect = row => ({
        supplementId: parseInt(row.dataset.supplementId),
        effectId: parseInt(row.dataset.effectId),
        cohenD: parseFloat(row.querySelector('input[name="cohenD"]').value),
        pearsonR: parseFloat(row.querySelector('input[name="pearsonR"]').value),
        pValue: parseFloat(row.querySelector('input[name="pValue"]').value)
    });

    const trialDesignOptions = [
        { value: '1', label: 'Meta-analysis', tier: 'A' },
        { value: '20', label: 'Systematic Review', tier: 'A' },
        { value: '21', label: 'RCT', tier: 'B' },
        { value: '22', label: 'Non-RCT', tier: 'B' },
        { value: '23', label: 'Cohort', tier: 'C' },
        { value: '24', label: 'Case-control', tier: 'C' },
        { value: '25', label: 'Cross-sectional', tier: 'C' },
        { value: '26', label: 'Case Report', tier: 'D' },
        { value: '27', label: 'Animal Study', tier: 'F' },
        { value: '28', label: 'In-vitro Study', tier: 'F' },
        { value: '29', label: 'Unknown', tier: 'F' }
    ];

    const journalSearch = (kw) => {
        setJournalState(prev => ({
            ...prev,
            query: { ...prev.query, keyword: kw}, // 검색은 보통 offset 초기화
        }));
    };

    const journalSuggestion = (keyword) => {
        return getJournalSuggests({keyword});
    }

    const addFilter = useCallback((type, id) => {
        setJournalState((s) => {
            const selected = s.query.filters[type] ?? [];
            const newSelected = selected.includes(id) ? selected.filter(v => v !== id) : [...selected, id];

            return {
                ...s,
                query: {
                    ...s.query,
                    offset: 0,
                    filters: { ...s.query.filters, [type]: newSelected },
                },
            };
        });
    }, []);


    return (
        <>
            <section>
                <div className="flex justify-between items-center">
                    <h2 id="index-1" data-svg="pill">논문 리스트</h2>
                    <div className="flex gap-2">
                        <SearchForm
                            id='search-form'
                            placeholder='논문 검색'
                            formClass='w-96'
                            inputClass='w-96'
                            submitFn={journalSearch}
                            changeFn={journalSuggestion}
                        />
                        <button onClick={toggleFold}>{journalState.fold ? '펼치기' : '접기'}</button>
                        <button onClick={toggleEdit}>{journalState.ui.editMode ? '보기모드' : '수정모드'}</button>
                        <select name="typeId" id="list-sort" onChange={(e) => changeSort(e)}>
                            <option value="title">제목순</option>
                            <option value="date">최신순</option>
                            <option value="participants">표본순</option>
                            <option value="durationDays">기간순</option>
                            <option value="score">점수순</option>
                        </select>
                        <button onClick={toggleAsc}>{journalState.query.asc ? '정방향' : '역방향'}</button>
                    </div>
                </div>
                <table>
                    <thead>
                    <tr>
                        <th>제목</th>
                        <th>연구설계</th>
                        <th>맹검</th>
                        <th>평행</th>
                        <th>기간</th>
                        <th>참가자</th>
                        <th>연관성분</th>
                        <th>연관효과</th>
                        <th>연관부작용</th>
                        <th>게재일</th>
                        {journalState.ui.editMode && <th>관리</th>}
                    </tr>
                    </thead>
                    <tbody>
                    {visibleJournals.map((journal, index) => {
                        const rowRef = rowRefs.current[index] ?? (rowRefs.current[index] = {current: null});
                        const supplements = [...new Set([...(journal.effects || []).map(e => e.supplementKorName), ...(journal.sideEffects || []).map(e => e.supplementKorName)])].join(', ');
                        const effects = [...new Set((journal.effects || []).map(e => e.effectKorName))].join(', ');
                        const sideEffects = [...new Set((journal.sideEffects || []).map(e => e.effectKorName))].join(', ');

                        return (
                            <tr key={journal.id} data-id={journal.id} ref={el => rowRefs.current[journalIndexMap.get(journal.id)] = { current: el }}>
                                <td>
                                    <a href={journal.link} target="_blank" rel="noreferrer">{journal.title}</a>
                                <Tooltip
                                    trigger={<span className='note'> [초록]</span>}
                                    content={<p>{journal.summary}</p>}
                                    placement='right'
                                    tail='true'
                                />
                                </td>
                                <td>{journalState.ui.editMode ? (
                                    <select
                                        name="trialDesign"
                                        defaultValue={journal.trialDesign?.id}
                                        className={journal.trialDesign?.tier[0]}
                                    >
                                        {trialDesignOptions.map(opt => (
                                            <option key={opt.value} value={opt.value}>{opt.label}</option>
                                        ))}
                                    </select>
                                ) : <span className={journal.trialDesign?.tier[0]}>{journal.trialDesign?.engName}</span>}</td>
                                <td>{journalState.ui.editMode ? (
                                    <select name="blind" defaultValue={journal.blind}>
                                        <option value="0">open-label</option>
                                        <option value="1">single-blind</option>
                                        <option value="2">double-blind</option>
                                    </select>
                                ) : journal.blind}</td>
                                <td>{journalState.ui.editMode ? (
                                    <select name="parallel" defaultValue={journal.parallel ? 'true' : 'false'}>
                                        <option value="true">O</option>
                                        <option value="false">X</option>
                                    </select>
                                ) : (journal.parallel ? 'O' : 'X')}</td>
                                <td>{journalState.ui.editMode ? (
                                    <>
                                        <input name="durationValue" type="number" defaultValue={journal.duration?.value ?? ''} />
                                        <select name="durationUnit" defaultValue={journal.duration?.unit}>
                                            <option value="day">day</option>
                                            <option value="week">week</option>
                                            <option value="month">month</option>
                                            <option value="year">year</option>
                                        </select>
                                    </>
                                ) : `${journal.duration?.value} ${journal.duration?.unit}`}</td>
                                <td>{journalState.ui.editMode ? (
                                    <input type="number" name="participants" defaultValue={journal.participants ?? ''} className="w-24" />
                                ) : journal.participants}</td>
                                <td>{supplements}</td>
                                <td>{effects}</td>
                                <td>{sideEffects}</td>
                                <td>{journal.date?.slice(0, 10)}</td>
                                {journalState.ui.editMode && (
                                    <td>
                                        <button className="tag-btn" data-id={journal.id}>태그</button>
                                        <button className="save-btn" onClick={() => handleSave(journal, rowRefs.current[journalIndexMap.get(journal.id)])}>저장</button>
                                        <button className="delete-btn" onClick={() => handleDelete(journal.id, journal.title)}>삭제</button>
                                    </td>
                                )}
                            </tr>
                        );
                    })}
                    </tbody>
                </table>
                <div className={`${journalState.ui.hasMore ? '' : 'hidden'} flex`}>
                  <button
                      id="load-more"
                      onClick={() => loadJournals('append')}
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
            </section>

            <section className="flex justify-between items-start mb-4 flex-wrap gap-5">
                <div>
                    <h2 id="index-2" data-svg="add" className="text-center">논문 추가</h2>
                    <form id="insert-form" className="items-start gap-2">
                        <input name="link" placeholder="링크는 필수 입력값입니다." className="w-48"/>
                        <div className="flex">
                            <input type="number" name="durationValue" placeholder="기간"/>
                            <select className="w-28" name="durationUnit">
                                <option value="day">일(day)</option>
                                <option value="week">주(week)</option>
                                <option value="month">월(month)</option>
                                <option value="year">년(year)</option>
                            </select>
                        </div>
                        <select name="trialDesign" className="w-48">
                            <option value="" className="F w-48">null</option>
                            {trialDesignOptions.map(opt => (
                                <option key={opt.value} value={opt.value} className={`${opt.tier} w-48`}>{opt.label}</option>
                            ))}
                        </select>
                        <select name="blind" className="w-32">
                            <option value="" className="w-32">null</option>
                            <option value="0" className="w-32">open-label</option>
                            <option value="1" className="B w-32">single-blind</option>
                            <option value="2" className="A w-32">double-blind</option>
                        </select>
                        <select name="parallel" className="w-32">
                            <option value="" className="w-32">null</option>
                            <option value="true" className="w-32">parallel</option>
                            <option value="false" className="w-32">cross-over</option>
                        </select>
                        <input type="number" name="participants" placeholder="참가자"/>
                        <button type="button" id="modal-open">태그</button>
                        <button type="submit">추가</button>
                    </form>
                </div>
                <div>
                    <h2 className="text-center">연구설계</h2>
                    <ul id="trialDesign-list"></ul>
                    <h2 className="line"></h2>
                    <ul id="blind-list">
                        <li data-id="2" className="A">Double-blind</li>
                        <li data-id="1" className="B">Single-blind</li>
                        <li data-id="0" >Open-label</li>
                    </ul>
                    <ul id="parallel-list">
                        <li data-id="true">Parallel</li>
                        <li data-id="false">Cross-over</li>
                    </ul>
                </div>
                <TagList
                  types={tagTypes}
                  selected={journalState.query.filters}
                  addFilter={addFilter}
                />
            </section>
            <section id="note">
                <a href="#inote-1">[1]</a>
                <div id="note-1">
                    링크 외에 다른 값들은 모르시면 빈값으로 넣어주셔도 무방합니다.
                    (크롤링 및 llm 분석으로 자동 매핑)
                    물론 직접 넣어주시는 편이 더 정확합니다.
                </div>
                <a href="#inote-2">[2]</a>
                <div id="note-2">
                    두 집단 간의 차이를 수치화한 값 (높을 수록 신뢰도 증가)
                </div>
                <a href="#inote-3">[3]</a>
                <div id="note-3">
                    두 변수의 상관 관계를 수치화한 값 (높을 수록 신뢰도 증가)
                </div>
                <a href="#inote-4">[4]</a>
                <div id="note-4">
                    효과가 우연히 발생했을 확률 (낮을 수록 신뢰도 증가)
                </div>
            </section>

            {/*<section id="modal" className="modal hidden">*/}
            {/*    <div className="modal-content">*/}
            {/*        <div id="modal-controller" className="flex-end">*/}
            {/*            <div className="flex justify-end gap-2">*/}

            {/*                <select name="typeId" id="modal-sort">*/}
            {/*                    <option value="korName">한글순</option>*/}
            {/*                    <option value="engName">영어순</option>*/}
            {/*                </select>*/}
            {/*                <button id="modal-delete">전체 해제</button>*/}
            {/*                <button id="modal-reset">초기화</button>*/}
            {/*                <button id="modal-close">닫기</button>*/}
            {/*            </div>*/}
            {/*        </div>*/}
            {/*        <div className="flex justify-between gap-5">*/}
            {/*            <div className="flex justify-between gap-5">*/}
            {/*                {['supplement', 'effect', 'sideEffect'].map(type => (*/}
            {/*                    <div key={type}>*/}
            {/*                        <h2 className="text-center">*/}
            {/*                            {type === 'supplement' && '성분 선택'}*/}
            {/*                            {type === 'effect' && '기대효과 선택'}*/}
            {/*                            {type === 'sideEffect' && '부작용 선택'}*/}
            {/*                        </h2>*/}
            {/*                        <div id={`${type}-checkboxes`} className="checkbox">*/}
            {/*                            {tags[type]?.map(tag => (*/}
            {/*                                <label key={tag.id} className="inline-flex items-center mr-2">*/}
            {/*                                    <input*/}
            {/*                                        type="checkbox"*/}
            {/*                                        name={`${type}[]`}*/}
            {/*                                        value={tag.id}*/}
            {/*                                        className="mr-1"*/}
            {/*                                        onChange={e => handleCheckboxChange(type, tag.id, e.target.checked)}*/}
            {/*                                    />*/}
            {/*                                    {tag.korName}*/}
            {/*                                </label>*/}
            {/*                            ))}*/}
            {/*                        </div>*/}
            {/*                    </div>*/}
            {/*                ))}*/}
            {/*            </div>*/}
            {/*            <div className="flex flex-col items-center">*/}

            {/*                <div className="w-full flex items-center justify-end gap-2">*/}
            {/*                    <h2 className="text-center">효과</h2>*/}
            {/*                    <button id="cash-insert">추가</button>*/}
            {/*                    <button id="cash-toggle-delete">삭제</button>*/}
            {/*                </div>*/}
            {/*                <table>*/}
            {/*                    <thead>*/}
            {/*                    <tr>*/}
            {/*                        <th>성분</th>*/}
            {/*                        <th>효과</th>*/}
            {/*                        <th>Cohen’s d<span id="inote-$1" className="tooltip"><a className="note" href="#note-2">[2]</a></span></th>*/}
            {/*                        <th>Pearson’s r<span id="inote-3" className="tooltip"><a className="note" href="#note-3">[3]</a></span></th>*/}
            {/*                        <th>p-value<span id="inote-4" className="tooltip"><a className="note" href="#note-4">[4]</a></span></th>*/}
            {/*                    </tr>*/}
            {/*                    </thead>*/}
            {/*                    <tbody id="mapping-cash"></tbody>*/}
            {/*                </table>*/}
            {/*                <p>*/}
            {/*                    성분과 효과를 하나씩 선택하고 효과 크기(성분이 피험자에게 끼치는 영향의 퍼센테이지), P-value(통계적 유의성)를 선택하면*/}
            {/*                    효과 크기, p-value, 연구 설계, 맹검 방법, 참가자, 기간에 따라 자동으로 점수가 산출됩니다. 이 중 입력되지 않은 수치들은*/}
            {/*                    크롤링된 초록을 llm에 넣어 산출된 값으로 자동 매핑되며 초록의 정보 부족, 혹은 ai 성능 한계로 null 값이 들어간 경우*/}
            {/*                    점수엔 기본값이 대신 반영됩니다.(효과크기:0%, 참가자:1명, 기간:1일, 맹검:오픈 라벨, 설계:시험관 실험)*/}
            {/*                </p>*/}
            {/*                <div className="line"></div>*/}
            {/*                <p>*/}
            {/*                    효과크기에는 여러 종류가 있는데 대표적으로 두 그룹 간의 차이를 나타내는 경우(Cohen’s d),*/}
            {/*                    두 변수 간의 상관계수를 나타내는 경우(Pearson’s r)로 나뉩니다.*/}
            {/*                    예를 들어 카페인 섭취가 불면증을 유발하는지에 대한 연구가 있다고 가정합시다.*/}
            {/*                    이 경우, 전자는 실험군이 대조군에 비해 불면이 유발된 사람이 얼마나 많은지를 나타낸다면*/}
            {/*                    후자는 카페인 섭취량과 불면 지수가 얼마나 비례하는지를 나타낸다고 할 수 있습니다.*/}
            {/*                    두 값 모두 입력받는게 베스트긴 하지만 현실적으로 너무 수고가 많이 들어가므로*/}
            {/*                    하나만 입력하시거나 아예 생략하셔도 무방합니다.(기본값으로 계산됨)*/}
            {/*                    또한 논문의 여러 수치들은 범위(ex 3~7주)로 설정되어 있는 경우가 많습니다. 이 경우*/}
            {/*                    산술평균, 중앙값, 혹은 본인이 적절하다고 생각하는 값을 자유롭게 넣어주시면 됩니다.*/}
            {/*                    맹검 여부(blind 여부) 역시 논문에서 명확하지 않은 경우, 최빈값이나 본인이 보기에 적절한 값을 선택해주셔도 괜찮습니다*/}
            {/*                </p>*/}
            {/*            </div>*/}
            {/*        </div>*/}
            {/*    </div>*/}
            {/*</section>*/}
        </>
    )
}
