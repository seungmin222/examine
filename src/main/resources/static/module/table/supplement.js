import {
    ArrayCheckboxesById,
    setupFoldToggle,
    setupSortTrigger,
    setupToggleButton,
    setupModalOpenClose,
    setupSearchForm,
    resetButton,
    selectList,
    renderSupplements,
    renderTags,
    renderModal,
    loadButton,
    supplementEvent,
    createNewAlarm,
    createParam,
    receiveData,
    insertData
} from '/util/index.js';


const supplementMap = new Map();
const state = { offset: 0, limit: 15 };
const tagState = { offset: 0, limit: 15 };
const modalState = { offset: 0, limit: 15 };

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
    resetButton("tags", "modal-reset", "modal-sort", ["type"], renderModal);

    // 모달 체크 해제
    document.getElementById('modal-delete').addEventListener('click', e => {
        loadTags();
    });

    //검색 폼
    document.getElementById('search-form').addEventListener('submit', async e => {
        e.preventDefault();
        const form = e.target;
        const param = createParam("list-sort", true, state.offset, state.limit);
        param.append('keyword',form.input.value);
        const data = await receiveData("/api/supplements/search", param);
        insertData(data, renderSupplements, [supplementMap]);
    });

    document.getElementById('tag-search-form').addEventListener('submit', async e => {
        e.preventDefault();
        const form = e.target;
        const param = createParam("tag-sort", true, tagState.offset, tagState.limit);
        param.append('keyword',form.input.value);
        const data = await receiveData("/api/tags", param);
        insertData(data, renderTags, []);
    });

    document.getElementById('modal-search-form').addEventListener('submit', async e => {
        e.preventDefault();
        const form = e.target;
        const param = createParam("modal-sort", true, modalState.offset, modalState.limit);
        param.append('keyword',form.input.value);
        const data = await receiveData("/api/tags", param);
        insertData(data, renderTags, []);
    });

    selectList(["type", "effect", "sideEffect", "tier"],filterByTag);
}


async function loadSupplements() {
    const param = createParam('list-sort', true, state.offset, state.limit);
    const data = await receiveData("/api/supplements", param);
    insertData(data, renderSupplements, [supplementMap]);
}


// 태그 로딩
async function loadTags() {
    const param = createParam('tag-sort', true, tagState.offset, tagState.limit);
    param.append('type',['type', 'effect', 'sideEffect']);
    const data = await receiveData("/api/tags", param);
    insertData(data, renderTags);
    insertData(data, renderModal);
}

// 태그 필터링
async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const param = createParam('list-sort', true, tagState.offset, tagState.limit);

    if (selected.length === 0) {
        await loadSupplements();
    } else {
        const typeIds = selected.filter(e => e.dataset.type === 'type').map(e => e.dataset.id);
        const effectIds = selected.filter(e => e.dataset.type === 'effect').map(e => e.dataset.id);
        const sideEffectIds = selected.filter(e => e.dataset.type === 'sideEffect').map(e => e.dataset.id);
        const tiers = selected.filter(e => e.dataset.type === 'tier').map(e => e.dataset.id);

        const params = new URLSearchParams();
        typeIds.forEach(id => params.append('typeIds', id));
        effectIds.forEach(id => params.append('effectIds', id));
        sideEffectIds.forEach(id => params.append('sideEffectIds', id));
        tiers.forEach(id => params.append('tiers', id));

        const data = await receiveData("/api/supplements", param);
        insertData(data, renderSupplements, [supplementMap]);
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
        createNewAlarm("성분이 추가되었습니다.");
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
        createNewAlarm("태그가 추가되었습니다.");
        form.reset();
        await loadTags();
    } else {
        alert("태그 추가 실패");
    }
});