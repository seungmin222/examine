import {
    createModalInner,
    createTagList,
    checkCheckboxesById,
    ArrayCheckboxesById
} from '/util/utils.js';

import {
    setupFoldToggle,
    setupSortTrigger,
    setupToggleButton,
    setupModalOpenClose,
    setupSearchForm,
    resetButton,
    selectList
} from '/util/event.js';

import {
    renderTagTable,
    renderTags,
    renderModal
} from '/util/render.js';

import {
    loadBasic
} from '/util/load.js';

import {
    tagTableEvent
} from '/util/tableEvent.js';

const supplementMap = new Map();

// 초기 로딩
document.addEventListener('DOMContentLoaded', async e => {

    await loadBasic(loadEffects);
    await loadEffects();
    await loadTags();
    // 테이블 수정, 삭제
    tagTableEvent(loadEffects, 'effect');

    // 접기 토글
    setupFoldToggle('toggle-fold', loadEffects);
    setupFoldToggle('tag-toggle-fold', loadTags);

    // 정렬
    setupSortTrigger('list-sort', loadEffects);
    setupSortTrigger('tag-sort', loadTags);

    // 삭제 / 수정 토글
    setupToggleButton('tag-toggle-delete', loadTags, '삭제', '삭제중'); // 수정버튼 없는 경우도 처리 가능 // 단독 토글일 경우 self로 전달

    // 모달 열고 닫기
    setupModalOpenClose('modal-open', 'modal-close', 'modal');

    //검색 폼
    setupSearchForm("tags", "tag-search-form", "tag-sort", ["supplement"], renderTags);
    setupSearchForm("effects", "search-form", "list-sort", ["effect"], renderTagTable); //api 주소 수정 필요

    selectList(["supplement"],filterByTag);
});


async function loadEffects() {
    const sort = document.getElementById('list-sort').value;
    const res = await fetch(`/api/tags/table?type=effect&sort=${encodeURIComponent(sort)}&direction=asc`);
    const allEffects = await res.json();
    renderTagTable(allEffects);
}

// 태그 로딩
async function loadTags() {
    const sort = document.getElementById('tag-sort').value;
    const allTypes = ['supplement'];

    const query = new URLSearchParams({
        type: allTypes.join(','), // 👉 type=positive,negative,type,...
        sort: sort,
        direction: 'asc'
    }).toString();

    const res = await fetch(`/api/tags?${query}`);
    const tagMap = await res.json();

    for (const [type, list] of Object.entries(tagMap)) {
        renderTags(type, list);
    }
}

// 태그 필터링
async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const sort = document.getElementById('list-sort').value;

    if (selected.length === 0) {
        loadEffects();
    } else {
        const supplementIds = selected.filter(e => e.dataset.type === 'supplement').map(e => e.dataset.id);
        const params = new URLSearchParams();
        supplementIds.forEach(id => params.append('typeIds', id));

        const res = await fetch(`/api/tags/table/filter?type=effect&${params.toString()}&sort=${sort}&direction=asc`);
        const filtered = await res.json();

        renderTagTable(filtered,supplementMap);
    }
}

// 태그 추가
document.getElementById('insert-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const data = {
        korName: form.korName.value,
        engName: form.engName.value,
        type: 'effect'
    };

    const res = await fetch('/api/tags', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (res.ok) {
        alert("태그가 추가되었습니다.");
        form.reset();
        await loadEffects();
    } else {
        alert("태그 추가 실패");
    }
});