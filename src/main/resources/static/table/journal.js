import {
    createModalInner,
    createTagList,
    checkCheckboxes,
    checkCheckboxesById,
    ArrayCheckboxesById,
    ObjectCheckboxesById,
    ArrayCheckboxesByName,
    resetModal
} from '/util/utils.js';

import {
    setupFoldToggle,
    setupSortTrigger,
    setupToggleButton,
    setupPairToggleButton,
    setupModalOpenClose,
    setupSearchForm,
    resetButton,
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
    loadBasic
} from '/util/load.js';

import {
    journalEvent
} from '/util/tableEvent.js';

// 전역 맵
const journalMap = new Map();

// 초기 로딩
document.addEventListener('DOMContentLoaded', async () => {

    await loadBasic(loadJournals);
    await loadJournals();
    await loadTags();
   // 테이블 클릭 이벤트리스너, 이벤트 위임이므로 한번만 추가
    journalEvent(journalMap, loadJournals);

    // 접기 토글
    setupFoldToggle('toggle-fold', loadJournals);
    setupFoldToggle('tag-toggle-fold', loadTags);

    // 정렬
    setupSortTrigger('list-sort', loadJournals);
    setupSortTrigger('tag-sort', loadTags);
    setupSortTrigger('modal-sort', loadTags);

    // 삭제 / 수정 토글
    setupToggleButton('cash-toggle-delete',()=>{}, '삭제', '삭제중')

    // 모달 열고 닫기
    setupModalOpenClose('modal-open', 'modal-close', 'modal');

    // 모달 리셋
    document.getElementById('modal-reset').addEventListener('click', e => {
        loadTags();
    });

    setupSearchForm("tags", "tag-search-form", "tag-sort", ["supplement", "positive", "negative"], renderTags);
    setupSearchForm("tags", "modal-search-form", "modal-sort", ["supplement", "positive", "negative"], renderModal);
    setupSearchForm("journals", "search-form", "list-sort", null, renderJournals);

    selectList(["trialDesign", "blind", "parallel", "supplement", "positive", "negative"],filterByTag);
    selectChange('journal-body');
    selectChange('insert-form');
});

async function loadJournals() {
    const sort = document.getElementById('list-sort').value;
    let dir = 'desc';
    if (sort==='title'){
       dir = 'asc';
    }
    const res = await fetch(`/api/journals?sort=${sort}&direction=${dir}`);
    const allJournals = await res.json();
    renderJournals(allJournals, journalMap);

}


// 태그 로딩
async function loadTags() {
    const sort = document.getElementById('tag-sort').value;
    const allTypes = ['positive', 'negative', 'supplement'];
    const allTierTypes = ['trialDesign'];
    for (let type of allTypes) {
        const res = await fetch(`/api/tags?type=${type}&sort=${encodeURIComponent(sort)}&direction=asc`);
        const list = await res.json();
        renderTags(type, list);
        renderModal(type, list);
    }
    for (let type of allTierTypes) {
        const res = await fetch(`/api/tags/tier?type=${type}`);
        const list = await res.json();
        renderTags(type, list);
        renderModal(type, list);
    }
     onlyOneCheckboxes(['supplement']);
     onlyOneCheckboxes(['positive', 'negative']);
     document.getElementById('mapping-cash').innerHTML = '';
}

// 태그 필터링
async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const sort = document.getElementById('list-sort').value;

    if (selected.length === 0) {
        loadJournals();
    } else {
        const trialDesign = selected.filter(e => e.dataset.type === 'trialDesign').map(e => e.dataset.id);
        const blind = selected.filter(e => e.dataset.type === 'blind').map(e => e.dataset.id);
        const parallel = selected.filter(e => e.dataset.type === 'parallel').map(e => e.dataset.id);
        const supplementIds = selected.filter(e => e.dataset.type === 'supplement').map(e => e.dataset.id);
        const effectIds = selected.filter(e => e.dataset.type === 'positive').map(e => e.dataset.id);
        const sideEffectIds = selected.filter(e => e.dataset.type === 'negative').map(e => e.dataset.id);
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

document.getElementById('cash-insert').addEventListener('click', async e => {
    e.preventDefault();

    const supplement = ArrayCheckboxesByName('supplement');
    const effect = ArrayCheckboxesByName('positive');
    const sideEffect = ArrayCheckboxesByName('negative');

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
    const wrapper = document.createElement('div');
    wrapper.classList.add('flex');

    const size = document.createElement('input');
    size.name = 'size';
    size.type = 'number';
    size.classList.add('wid-60px');
    size.value = "0";

    const percent = document.createElement('span');

    wrapper.appendChild(size);
    wrapper.appendChild(percent);
    td3.appendChild(wrapper);

    row.appendChild(td1);
    row.appendChild(td2);
    row.appendChild(td3);

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

   const effects = [...document.querySelectorAll('.effect-cash')].map(e => ({
     supplementId: parseInt(e.dataset.supplementId),
     effectId: parseInt(e.dataset.effectId),
     size: parseFloat(e.querySelector('input[name="size"]').value)
   }));

   const sideEffects = [...document.querySelectorAll('.sideEffect-cash')].map(e => ({
     supplementId: parseInt(e.dataset.supplementId),
     sideEffectId: parseInt(e.dataset.effectId),
     size: parseFloat(e.querySelector('input[name="size"]').value)
   }));

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


