import {
    ArrayCheckboxesByName,
    renderEffectCache
} from '/util/utils.js';

import {
    setupFoldToggle,
    setupSortTrigger,
    setupToggleButton,
    setupModalOpenClose,
    setupSearchForm,
    selectList,
    onlyOneCheckboxes,
    selectChange,
} from '/util/event.js';

import {
    renderJournals,
    renderTags,
    renderModal
} from '/util/render.js';

import {
    loadButton
} from '/util/load.js';

import {
    journalEvent
} from '/util/tableEvent.js';

// 전역 맵
const journalMap = new Map();

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
        if (item) renderEffectCache(item);
    });

    document.getElementById('modal-delete').addEventListener('click', e => {
        loadTags();
    });

    setupSearchForm("tags", "tag-search-form", "tag-sort", ["supplement", "effect", "sideEffect"], renderTags);
    setupSearchForm("tags", "modal-search-form", "modal-sort", ["supplement", "effect", "sideEffect"], renderModal);
    setupSearchForm("journals", "search-form", "list-sort", null, renderJournals);

    selectList(["trialDesign", "blind", "parallel", "supplement", "effect", "sideEffect"], filterByTag);
    selectChange('journal-body');
    selectChange('insert-form');
}

async function loadJournals() {
    const sort = document.getElementById('list-sort')?.value;
    let dir = 'desc';
    if (sort === 'title'){
        dir = 'asc';
    }
    const res = await fetch(`/api/journals?sort=${sort}&direction=${dir}`);
    const allJournals = await res.json();
    renderJournals(allJournals, journalMap);

}


// 태그 로딩
async function loadTags() {
    const sort = document.getElementById('tag-sort')?.value;
    const allTypes = ['effect', 'sideEffect', 'supplement', 'trialDesign'];
    const query = new URLSearchParams({
        type: allTypes.join(','), // 👉
        sort: sort,
        direction: 'asc'
    }).toString();

    const res = await fetch(`/api/tags?${query}`);
    const tagMap = await res.json();

    for (const [type, list] of Object.entries(tagMap)) {
        renderTags(type, list);
        if(type !== 'trialDesign'){
            renderModal(type, list);
        }
    }
    onlyOneCheckboxes(['supplement']);
    onlyOneCheckboxes(['effect', 'sideEffect']);
    document.getElementById('mapping-cash').innerHTML = '';
}

// 태그 필터링
async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const sort = document.getElementById('list-sort')?.value;

    if (selected.length === 0) {
        loadJournals();
    } else {
        const trialDesign = selected.filter(e => e.dataset.type === 'trialDesign').map(e => e.dataset.id);
        const blind = selected.filter(e => e.dataset.type === 'blind').map(e => e.dataset.id);
        const parallel = selected.filter(e => e.dataset.type === 'parallel').map(e => e.dataset.id);
        const supplementIds = selected.filter(e => e.dataset.type === 'supplement').map(e => e.dataset.id);
        const effectIds = selected.filter(e => e.dataset.type === 'effect').map(e => e.dataset.id);
        const sideEffectIds = selected.filter(e => e.dataset.type === 'sideEffect').map(e => e.dataset.id);
        const params = new URLSearchParams();

        trialDesign.forEach(id => params.append('trialDesign', id));
        blind.forEach(id => params.append('blind', id));
        parallel.forEach(id => params.append('parallel', id));
        supplementIds.forEach(id => params.append('supplementIds', id));
        effectIds.forEach(id => params.append('effectIds', id));
        sideEffectIds.forEach(id => params.append('sideEffectIds', id));

        const res = await fetch(`/api/journals/filter?${params.toString()}&sort=${sort}&direction=asc`);
        const filtered = await res.json();

        renderJournals(filtered, journalMap);

    }
}

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
    console.error("응답 본문", text);

    if (res.ok) {
        alert("논문이 추가되었습니다.");
        form.reset();
        loadJournals();
    } else {
        alert("추가 실패");
    }
});


