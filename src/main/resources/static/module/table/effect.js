import {
    setupFoldToggle,
    setupSortTrigger,
    setupToggleButton,
    setupModalOpenClose,
    setupSearchForm,
    selectList
} from '/util/event.js';

import {
    renderTagTable,
    renderTags,
} from '/util/render.js';

import {
    loadButton
} from '/util/load.js';

import {
    tagTableEvent
} from '/util/tableEvent.js';

const params = new URLSearchParams(window.location.search);
const effectType = params.get('type');
console.log("📦 effectType:", effectType);

// 초기 로딩
export async function init() {

    document.title = effectType;
    const subTitle = document.getElementById('index-1');
    if(effectType === 'types') {
        subTitle.textContent = '분류 리스트';
    }
    else if (effectType === 'sideEffect') {
        subTitle.textContent = '부작용 리스트';
    }
    await loadButton(loadEffects);
    await loadEffects();
    await loadTags();
    // 테이블 수정, 삭제
    tagTableEvent(loadEffects, `${effectType}`);

    // 접기 토글
    setupFoldToggle('toggle-fold', loadEffects);
    setupFoldToggle('tag-toggle-fold', loadTags);

    // 정렬
    setupSortTrigger('list-sort', loadEffects);
    setupSortTrigger('tag-sort', loadTags);

    //검색 폼
    setupSearchForm("tags", "tag-search-form", "tag-sort", ["supplement"], renderTags);
    setupSearchForm(`${effectType}`, "search-form", "list-sort", [`${effectType}`], renderTagTable); //api 주소 수정 필요

    selectList(["supplement"],filterByTag);
}


async function loadEffects() {
    const sort = document.getElementById('list-sort').value;
    const res = await fetch(`/api/tags/table?type=${effectType}&sort=${encodeURIComponent(sort)}&direction=asc`);
    const allEffects = await res.json();
    renderTagTable(allEffects, effectType, 'tag-body');
}

// 태그 로딩
async function loadTags() {
    const sort = document.getElementById('tag-sort').value;
    const allTypes = ['supplement'];

    const query = new URLSearchParams({
        type: allTypes.join(','),
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

        const res = await fetch(`/api/tags/table/filter?type=${effectType}&${params.toString()}&sort=${sort}&direction=asc`);
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
        type: `${effectType}`
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