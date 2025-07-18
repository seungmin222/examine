import {
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
    renderSupplements,
    renderTags,
    renderModal
} from '/util/render.js';

import {
    loadButton
} from '/util/load.js';

import {
    supplementEvent
} from '/util/tableEvent.js';

const supplementMap = new Map();

// 초기 로딩
export async function init() {

    await loadButton(loadSupplements);
    await loadSupplements();
    await loadTags();
    // 테이블 수정, 삭제
    supplementEvent(supplementMap, loadSupplements);

    // 접기 토글
    setupFoldToggle('toggle-fold', loadSupplements);
    setupFoldToggle('tag-toggle-fold', loadTags);

    // 정렬
    setupSortTrigger('list-sort', loadSupplements);
    setupSortTrigger('tag-sort', loadTags);
    setupSortTrigger('modal-sort', loadTags);

    // 삭제 / 수정 토글
    setupToggleButton('tag-toggle-delete', loadTags, '삭제', '삭제중'); // 수정버튼 없는 경우도 처리 가능 // 단독 토글일 경우 self로 전달

    // 모달 열고 닫기
    setupModalOpenClose('modal-open', 'modal-close', 'modal');
    resetButton("tags", "modal-reset", "modal-sort", ["type", "effect", "sideEffect"], renderModal);

    // 모달 체크 해제
    document.getElementById('modal-delete').addEventListener('click', e => {
        loadTags();
    });

    //검색 폼
    setupSearchForm("tags", "tag-search-form", "tag-sort", ["type", "effect", "sideEffect"], renderTags);
    setupSearchForm("tags", "modal-search-form", "modal-sort", ["type", "effect", "sideEffect"], renderModal);
    setupSearchForm("supplements", "search-form", "list-sort", null, renderSupplements);

    selectList(["type", "effect", "sideEffect", "tier"],filterByTag);
}


async function loadSupplements() {
    const sort = document.getElementById('list-sort').value;
    const res = await fetch(`/api/supplements?sort=${sort}&direction=asc`);
    const allSupplements = await res.json();
    renderSupplements(allSupplements,supplementMap);
}


// 태그 로딩
async function loadTags() {
    const sort = document.getElementById('tag-sort').value;
    const allTypes = ['type', 'effect', 'sideEffect'];

    const query = new URLSearchParams({
        type: allTypes.join(','), //
        sort: sort,
        direction: 'asc'
    }).toString();

    const res = await fetch(`/api/tags?${query}`);
    const tagMap = await res.json();

    for (const [type, list] of Object.entries(tagMap)) {
        renderTags(type, list);
        renderModal(type, list);
    }
}

// 태그 필터링
async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const sort = document.getElementById('list-sort').value;

    if (selected.length === 0) {
        loadSupplements();
    } else {
        const typeIds = selected.filter(e => e.dataset.type === 'type').map(e => e.dataset.id);
        const effectIds = selected.filter(e => e.dataset.type === 'effect').map(e => e.dataset.id);
        const sideEffectIds = selected.filter(e => e.dataset.type === 'sideEffect').map(e => e.dataset.id);
        const tiers = selected.filter(e => e.dataset.type === 'tier').map(e => e.dataset.id);
        //selected 개수 많아지면 filter 말고 append로 한번에 처리
        const params = new URLSearchParams();
        typeIds.forEach(id => params.append('typeIds', id));
        effectIds.forEach(id => params.append('effectIds', id));
        sideEffectIds.forEach(id => params.append('sideEffectIds', id));
        tiers.forEach(id => params.append('tiers', id));

        const res = await fetch(`/api/supplements/filter?${params.toString()}&sort=${sort}&direction=asc`);
        const filtered = await res.json();

        renderSupplements(filtered,supplementMap);
    }
}

// 성분 추가
document.getElementById('insert-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;

    const types = ArrayCheckboxesById('type');

    const data = {
        korName: form.korName.value,
        engName: form.engName.value,
        types,
        dosageValue: form.dosageValue.value,
        dosageUnit: form.dosageUnit.value,
        cost: parseFloat(form.cost.value),
    };

    const res = await fetch('/api/supplements', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (res.ok) {
        alert("성분이 추가되었습니다.");
        form.reset();
        // 효과 체크박스 및 셀렉트 초기화
        document.querySelectorAll('#effect-checkboxes div').forEach(wrapper => {
            const checkbox = wrapper.querySelector('input[type="checkbox"]');
            const select = wrapper.querySelector('select');

            checkbox.checked = false;
            select.value = 'null'; // 기본 옵션으로 되돌림
        });
        document.querySelectorAll('#sideEffect-checkboxes div').forEach(wrapper => {
            const checkbox = wrapper.querySelector('input[type="checkbox"]');
            const select = wrapper.querySelector('select');

            checkbox.checked = false;
            select.value = 'null'; // 기본 옵션으로 되돌림
        });
        await loadSupplements();
    } else {
        alert("추가 실패");
    }
});

// 태그 추가
document.getElementById('tag-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const data = {
        name: form.tagName.value,
        type: form.tagType.value
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
        await loadTags();
    } else {
        alert("태그 추가 실패");
    }
});