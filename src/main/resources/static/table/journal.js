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
    onlyOneCheckboxes
} from '/util/eventUtils.js';

import {
    loadBasic
} from '/util/load.js';

// 전역 맵
const journalMap = new Map();

// 초기 로딩
document.addEventListener('DOMContentLoaded', async () => {

    await loadBasic();
    await loadJournals();
    await loadTags();

    // 접기 토글
    setupFoldToggle('toggle-fold', loadJournals);
    setupFoldToggle('tag-toggle-fold', loadTags);

    // 정렬
    setupSortTrigger('list-sort', loadJournals);
    setupSortTrigger('tag-sort', loadTags);
    setupSortTrigger('modal-sort', loadTags);

    // 삭제 / 수정 토글
    setupPairToggleButton('toggle-delete', 'toggle-change', loadJournals);
    setupToggleButton('cash-toggle-delete')

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
});

async function loadJournals() {
    const sort = document.getElementById('list-sort').value;
    const res = await fetch(`/api/journals?sort=${sort}&direction=asc`);
    const allJournals = await res.json();
    renderJournals(allJournals);
}

function renderJournals(list) {
    const tbody = document.getElementById('journal-body');
    tbody.innerHTML = '';
    journalMap.clear();
    const folded = document.getElementById('fold-toggle')?.classList.contains('folded');
    const shown = folded ? list.slice(0, 5) : list;
    const editMode = document.getElementById('toggle-change')?.classList.contains('execute');
    const deleteMode = document.getElementById('toggle-delete')?.classList.contains('execute');

    const trialDesignOptions = [ // 추후 하드코딩 말고 api로 받아서 전역 map으로 설정하기
      { value: '1', label: 'Meta-analysis', tier: 'A' },
      { value: '20', label: 'Systematic Review', tier: 'A' },
      { value: '21', label: 'RCT', tier: 'B' },
      { value: '22', label: 'Non-RCT', tier: 'B' },
      { value: '23', label: 'Cohort', tier: 'C' },
      { value: '24', label: 'Case-control', tier: 'C' },
      { value: '25', label: 'Cross-sectional', tier: 'C' },
      { value: '26', label: 'Case Report', tier: 'D' },
      { value: '27', label: 'Animal Study', tier: 'F' },
      { value: '28', label: 'In-vitro Study', tier: 'F' }
    ];



    shown.forEach(item => {
        journalMap.set(item.id, item);
        const row = document.createElement('tr');
        row.dataset.id = item.id; // ✅ 여기 추가

        const supplementSet = new Set([
          ...(item.effects?.map(e => e.supplementName) || []),
          ...(item.sideEffects?.map(e => e.supplementName) || [])
        ]);
        const supplements = [...supplementSet].join(', ');
        const effects = item.effects?.map(e => `${e.effectName} (${e.size}%)`).join(', ') || '';
        const sideEffects = item.sideEffects?.map(e => `${e.sideEffectName} (${e.size}%)`).join(', ') || '';

        const trialDesignSelectOptions = trialDesignOptions.map(opt => `
              <option value="${opt.value}" class="${opt.tier}" ${item.trialDesign?.id === opt.value ? 'selected' : ''}>
                ${opt.label}
              </option>
            `).join('');

        row.innerHTML = `
          <td class="tooltip" data-position="right" data-tooltip="${item.summary}">
              <a target="_blank" rel="noopener noreferrer" href="${item.link}"> ${item.title}</a>
          </td>
          <td class="${item.trialDesign?.tier[0]}">
            ${editMode
               ? `<select name="trialDesign" class="w-32">
                      ${trialDesignSelectOptions}
                   </select>`
               : `${item.trialDesign?.name}`
             }
          </td>
          <td>
            ${editMode
               ? `<select name="blind" class="w-32">
                     <option value="open-label" ${item.blind === 'open-label' ? 'selected' : ''}>open-label</option>
                     <option value="single-blind" ${item.blind === 'single-blind' ? 'selected' : ''}>single-blind</option>
                     <option value="double-blind" ${item.blind === 'double-blind' ? 'selected' : ''}>double-blind</option>
                  </select>`
               : `${item.blind}`
             }
          </td>
          <td>
             ${editMode
                ? `<select name="parallel" class="w-20">
                      <option value="true" ${item.parallel === 'true' ? 'selected' : ''}>parallel</option>
                      <option value="false" ${item.parallel === 'false' ? 'selected' : ''}>cross-over</option>
                   </select>`
                : `${item.parallel}`
              }
          </td>
          <td>
            ${editMode
              ? `<input name="duration-value" value="${item.duration.value}"/>
                 <select name="duration-unit">
                    <option value="day" ${item.duration.unit === 'day' ? 'selected' : ''}>day</option>
                    <option value="month" ${item.duration.unit === 'month' ? 'selected' : ''}>month</option>
                    <option value="year" ${item.duration.unit === 'year' ? 'selected' : ''}>year</option>
                 </select>`
              : `${item.duration.value} ${item.duration.unit}`
            }
          </td>
          <td>
            ${editMode
              ? `<input type="number" value="${item.participants}" class="w-24" name="participants"/>`
              : item.participants}
          </td>
          <td>${supplements}</td>
          <td>${effects}</td>
          <td>${sideEffects}</td>
          <td>${item.date}</td>
          ${editMode ? `<td>
             <div class="edit-group">
                <button class="modal-btn" data-id="${item.id}">태그</button>
                <button class="save-btn" data-id="${item.id}">저장</button>
             </div>
          </td>` : ''}
        `;

        tbody.appendChild(row);
    });
}

document.getElementById('journal-body').addEventListener('click', async e => {
    const row = e.target.closest('tr');
    const itemId = Number(row?.dataset.id);
    const item = journalMap.get(itemId);
    const modal = document.getElementById('modal');
    const modalId = modal?.dataset.id;
    if (!row||!itemId) return;

    // 삭제 모드
    if (document.getElementById('toggle-delete')?.classList.contains('execute')) {
        e.preventDefault();
        const title = row.querySelector('a')?.textContent.trim();
        if (confirm(`'${title}' 논문을 삭제할까요?`)) {
            await fetch(`/api/journals/${itemId}`, {
                method: 'DELETE'
            });
            await loadJournals();
        }
        return;
    }
    // 태그 모달
    if (e.target.classList.contains('modal-btn')) {
        if (itemId != modalId) {
           modal.dataset.id = itemId;
           renderAllEffectCache(item);
        }
        modal.style.display = 'block';
    }

    // 저장 버튼
    if (e.target.classList.contains('save-btn')) {
        let effects, sideEffects;

    if (itemId != modalId) {
        effects = item.effects;
        sideEffects = item.sideEffects;
    } else {
        effects = [...document.querySelectorAll('.effect-cash')].map(e => ({
            supplementId: parseInt(e.dataset.supplementId),
            effectId: parseInt(e.dataset.effectId),
            size: parseFloat(e.querySelector('input[name="size"]').value)
        }));

        sideEffects = [...document.querySelectorAll('.sideEffect-cash')].map(e => ({
            supplementId: parseInt(e.dataset.supplementId),
            sideEffectId: parseInt(e.dataset.effectId),
            size: parseFloat(e.querySelector('input[name="size"]').value)
        }));
    }

    const trialDesign = {
        id: parseInt(row.querySelector('[name="trialDesign"]').value)
    };

    const duration = {
        value: parseInt(row.querySelector('[name="duration-value"]').value),
        unit: row.querySelector('[name="duration-unit"]').value
    };

    const updated = {
        title: item.title,
        link: item.link,
        trialDesign,
        blind: row.querySelector('[name="blind"]').value,
        parallel: row.querySelector('[name="parallel"]').value,
        duration,
        participants: parseInt(row.querySelector('[name="participants"]').value),
        effects,
        sideEffects,
        summary: item.summary,
        date: item.date
    };

    const res = await fetch(`/api/journals/${itemId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(updated)
    });

    if (res.ok) {
        alert("논문이 수정되었습니다.");
        //모달 초기화 및 테이블 재랜더링
        loadTags();
        loadJournals();
        modal.dataset.id = "0";
    } else {
        alert("수정 실패");
    }
}



});



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

function renderTags(type, list) {
    let tagList = document.getElementById(`${type}-list`);
    if (!tagList) {
        console.warn(`❌ 알 수 없는 type: ${type}`);
        return;
    }
    tagList.innerHTML = '';

    const folded = document.getElementById('tag-toggle-fold')?.classList.contains('folded');
    const shown = folded ? list.slice(0, 5) : list;

    const items = createTagList(type, shown);
    items.forEach(li => tagList.appendChild(li));

}


function renderModal(type, list) {
    const sort = document.getElementById('modal-sort').value;
    const container = document.getElementById(`${type}-checkboxes`);
    if (container) {
        container.innerHTML = '';
        list.forEach(tag => {
            const item = createModalInner(tag, type);
            container.appendChild(item);
        });
    } else {
        console.warn(`❌ 알 수 없는 type: ${type}`);
        return;
    }
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

        renderJournals(filtered);

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

   const trialDesign = {
     id: form.trialDesign.value
   };

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

    const duration = {
        value: form['duration-value'].value,
        unit: form['duration-unit'].value
    };

    const data = {
        link: form.link.value,
        trialDesign,
        blind: form.blind.value,
        parallel: form.parallel.value,
        duration,
        participants: form.participants.value,
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

function renderAllEffectCache(item) {
  const container = document.getElementById('mapping-cash');
  container.innerHTML = ''; // ✅ 초기화

  // ✅ 효과(effect) 캐시
  item.effects?.forEach(effect => {
    const row = document.createElement('tr');
    row.classList.add('effect-cash');
    row.dataset.supplementId = effect.supplementId;
    row.dataset.effectId = effect.effectId;

    const td1 = document.createElement('td');
    td1.textContent = effect.supplementName;

    const td2 = document.createElement('td');
    td2.textContent = effect.effectName;

    const td3 = document.createElement('td');
    const wrapper = document.createElement('div');
    wrapper.classList.add('flex');

    const size = document.createElement('input');
    size.name = 'size';
    size.type = 'number';
    size.classList.add('wid-60px');
    size.value = effect.size ?? '';

    const percent = document.createElement('span');
    percent.textContent = ' %';

    wrapper.appendChild(size);
    wrapper.appendChild(percent);
    td3.appendChild(wrapper);

    row.appendChild(td1);
    row.appendChild(td2);
    row.appendChild(td3);

    container.appendChild(row);
  });

  // ✅ 부작용(sideEffect) 캐시
  item.sideEffects?.forEach(effect => {
    const row = document.createElement('tr');
    row.classList.add('sideEffect-cash');
    row.dataset.supplementId = effect.supplementId;
    row.dataset.effectId = effect.sideEffectId;

    const td1 = document.createElement('td');
    td1.textContent = effect.supplementName;

    const td2 = document.createElement('td');
    td2.textContent = effect.sideEffectName;

    const td3 = document.createElement('td');
    const wrapper = document.createElement('div');
    wrapper.classList.add('flex');

    const size = document.createElement('input');
    size.name = 'size';
    size.type = 'number';
    size.classList.add('wid-60px');
    size.value = effect.size ?? '';

    const percent = document.createElement('span');
    percent.textContent = ' %';

    wrapper.appendChild(size);
    wrapper.appendChild(percent);
    td3.appendChild(wrapper);

    row.appendChild(td1);
    row.appendChild(td2);
    row.appendChild(td3);

    container.appendChild(row);
  });
}
