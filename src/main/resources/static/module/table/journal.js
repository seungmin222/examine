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
} from '/util/index.js';


// 전역 맵
const journalMap = new Map();
const state = { offset: 0, limit: 1, method: "sort", keyword: '' };
const tagState = { offset: 0, limit: 1, method: "sort", keyword: '' };
const modalState = { offset: 0, limit: 1, method: "sort", keyword: '' };

let globalParam = new URLSearchParams();

// 초기 로딩
export async function init() {

    await loadButton(loadJournals);
    await loadJournals();
    await loadTags();

    journalEvent(journalMap, loadJournals, loadTags);
    setupFoldToggle('toggle-fold', loadJournals);
    setupFoldToggle('tag-toggle-fold', loadTags);
    setupSortTrigger('list-sort', loadJournals);
    setupSortTrigger('tag-sort', loadTags);
    setupSortTrigger('modal-sort', loadTags);
    setupToggleButton('cash-toggle-delete', () => {}, '삭제', '삭제중');
    setupModalOpenClose('modal-open', 'modal-close', 'modal');

    document.getElementById('modal-reset').addEventListener('click', e => {
        const itemId = parseInt(document.getElementById('modal-btn').dataset.id);
        const item = journalMap.get(itemId);
        if (item) createEffectCache(item);
    });

    document.getElementById('modal-delete').addEventListener('click', e => {
        loadTags();
    });

    document.getElementById('search-form').addEventListener('submit', async e => {
        e.preventDefault();
        const form = e.target;

        state.offset = 0;
        state.method = 'search';
        state.keyword = form.input.value;

        globalParam = createParam("list-sort", true, state.offset, state.limit);
        globalParam.append('keyword', state.keyword);

        const data = await receiveData("/api/journals/search", globalParam);
        showLoadBtn(data.hasMore);
        insertData(data.data, renderJournals, [journalMap, true]);
    });


    document.getElementById('tag-search-form').addEventListener('submit', async e => {
        e.preventDefault();
        const form = e.target;
        const param = createParam("tag-sort", true, tagState.offset, tagState.limit);
        param.append('keyword',form.input.value);
        const data = await receiveData("/api/tags", param);
        showLoadBtn(data.hasMore);
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


    selectList(["trialDesign", "blind", "parallel", "supplement", "effect", "sideEffect"], filterByTag);
    selectChange('journal-body');
    selectChange('insert-form');
}

async function loadJournals() {
    state.offset = 0;
    state.method = 'sort';

    globalParam = createParam('list-sort', true, state.offset, state.limit);

    const data = await receiveData("/api/journals", globalParam);
    showLoadBtn(data.hasMore);
    insertData(data.data, renderJournals, [journalMap, true]);
}



// 태그 로딩
async function loadTags() {

    const param = createParam('tag-sort', true, tagState.offset, tagState.limit);
    param.append('type',['effect','sideEffect', 'supplement', 'trialDesign']);
    const data = await receiveData("/api/tags", param);
    insertData(data, renderTags);
    insertData(data, renderModal);

    onlyOneCheckboxes(['supplement']);
    onlyOneCheckboxes(['effect', 'sideEffect']);
    document.getElementById('mapping-cash').innerHTML = '';
}

// 태그 필터링
async function filterByTag() {
    const limit = state.offset + 1; //기존 랜더링 된 것들 중 필터링
    state.offset = 0;
    state.method = 'filter';

    globalParam = createParam('list-sort', true, state.offset, limit);

    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    if (selected.length === 0) return await loadJournals();

    // 항목별 param 설정
    const getIds = type => selected.filter(e => e.dataset.type === type).map(e => e.dataset.id);
    for (const [key, type] of [
        ['trialDesign', 'trialDesign'],
        ['blind', 'blind'],
        ['parallel', 'parallel'],
        ['supplementIds', 'supplement'],
        ['effectIds', 'effect'],
        ['sideEffectIds', 'sideEffect'],
    ]) {
        getIds(type).forEach(id => globalParam.append(key, id));
    }

    const data = await receiveData("/api/journals/filter", globalParam);
    showLoadBtn(data.hasMore);
    insertData(data.data, renderJournals, [journalMap, true]);
}


document.getElementById('load-more').addEventListener('click', async (e) => {
    state.offset += state.limit;
    globalParam.set('offset', state.offset);

    const endpoint = {
        sort: "/api/journals",
        search: "/api/journals/search",
        filter: "/api/journals/filter"
    }[state.method];

    const data = await receiveData(endpoint, globalParam);
    showLoadBtn(data.hasMore);
    insertData(data.data, renderJournals, [journalMap, false]);

});

document.getElementById('cash-insert')?.addEventListener('click', async e => {
    e.preventDefault();

    const supplement = ArrayCheckboxesByName('supplement');
    const effect = ArrayCheckboxesByName('effect');
    const sideEffect = ArrayCheckboxesByName('sideEffect');

    if (supplement.length !== 1 || effect.length + sideEffect.length !== 1) {
        alert("성분과 효과를 하나씩 선택해 주세요.");
        return;
    }

    const type = effect.length === 1 ? 'effect' : 'sideEffect';
    const selected = effect.length === 1 ? effect[0] : sideEffect[0];
    const supplementId = supplement[0].id;
    const effectId = selected.id;

    // ✅ 중복 검사
    const existing = document.querySelectorAll(`#mapping-cash .${type}-cash`);
    const isDuplicate = Array.from(existing).some(row =>
        row.dataset.supplementId === String(supplementId) &&
        row.dataset.effectId === String(effectId)
    );
    if (isDuplicate) {
        alert('이미 동일한 조합이 추가되어 있습니다.');
        return;
    }

    // ✅ 캐시 생성 (테이블 행 기반)
    const row = document.createElement('tr');
    row.classList.add(`${type}-cash`);
    row.dataset.supplementId = supplementId;
    row.dataset.effectId = effectId;

    const td1 = document.createElement('td');
    td1.textContent = supplement[0].name;

    const td2 = document.createElement('td');
    td2.textContent = selected.name;

    const td3 = document.createElement('td');
    const cohenD = document.createElement('input');
    cohenD.name = 'cohenD';
    cohenD.type = 'number';
    cohenD.classList.add('w-16');
    cohenD.value = '';
    cohenD.step = '0.1';
    cohenD.placeholder = "null";
    td3.appendChild(cohenD);

    const td4 = document.createElement('td');
    const pearsonR = document.createElement('input');
    pearsonR.name = 'pearsonR';
    pearsonR.type = 'number';
    pearsonR.classList.add('w-16');
    pearsonR.value = '';
    pearsonR.step = '0.1';
    pearsonR.placeholder = "null";
    td4.appendChild(pearsonR);

    const td5 = document.createElement('td');
    const pValue = document.createElement('input');
    pValue.name = 'pValue';
    pValue.type = 'number';
    pValue.classList.add('w-24');
    pValue.value = '';
    pValue.step = "0.001";
    pValue.placeholder = "null";
    td5.appendChild(pValue);

    row.appendChild(td1);
    row.appendChild(td2);
    row.appendChild(td3);
    row.appendChild(td4);
    row.appendChild(td5);

    document.getElementById('mapping-cash').appendChild(row);
});

//캐시 삭제
document.getElementById('mapping-cash').addEventListener('click', e => {
    const deleteMode = document.getElementById('cash-toggle-delete').classList.contains('execute');
    if (deleteMode){
        if (confirm('캐시를 삭제할까요?')) {
            const target = e.target.closest('.effect-cash, .sideEffect-cash');
            if (target) {
                target.remove();
            }
        }
    }
});

// 논문 추가
document.getElementById('insert-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;

    let effects, sideEffects;

    if(document.getElementById('modal').dataset.id==='') {
        effects = [...document.querySelectorAll('.effect-cash')].map(c => ({
            supplementId: parseInt(c.dataset.supplementId),
            effectId: parseInt(c.dataset.effectId),
            cohenD: parseFloat(c.querySelector('input[name="cohenD"]')?.value),
            pearsonR: parseFloat(c.querySelector('input[name="pearsonR"]')?.value),
            pValue: parseFloat(c.querySelector('input[name="pValue"]')?.value)
        }));

        sideEffects = [...document.querySelectorAll('.sideEffect-cash')].map(c => ({
            supplementId: parseInt(c.dataset.supplementId),
            effectId: parseInt(c.dataset.effectId),
            cohenD: parseFloat(c.querySelector('input[name="cohenD"]')?.value),
            pearsonR: parseFloat(c.querySelector('input[name="pearsonR"]')?.value),
            pValue: parseFloat(c.querySelector('input[name="pValue"]')?.value)
        }));
    }

    const data = {
        link: form.link.value,
        trialDesignId: parseInt(form.trialDesign.value),
        blind: parseInt(form.blind.value),
        parallel: form.parallel.value,
        durationValue: parseInt(form.durationValue.value),
        durationUnit: form.durationUnit.value,
        participants:  parseInt(form.participants.value),
        effects,
        sideEffects,
    };

    console.log("보내는 데이터", data);

    const res = await fetch(`/api/journals`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    const text = await res.text();
    console.log("응답 본문", text);

    if (res.ok) {
        createNewAlarm("논문이 추가되었습니다.");
        form.reset();
        await loadJournals();
    } else {
        alert("추가 실패");
    }
});


