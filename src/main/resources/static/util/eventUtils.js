import {
  createTooltip,
  resetEventListener,
  ArrayCheckboxesById,
  checkCheckboxesById,
  renderEffectCache
} from '/util/utils.js';

function setupFoldToggle(buttonId, targetFn) {
  const btn = document.getElementById(buttonId);
  if (!btn) return;

  btn.addEventListener('click', e => {
    e.target.classList.toggle('folded');
    e.target.textContent = e.target.classList.contains('folded') ? '펼치기' : '접기';
    targetFn();
  });
}

function setupSortTrigger(selectId, handler) {
  const select = document.getElementById(selectId);
  if (!select) return;

  select.addEventListener('change', handler);
}

function setupToggleButton(buttonId, onText='삭제', offText='삭제중') {
  const Btn = document.getElementById(buttonId);

  if (!Btn) return;

  Btn.addEventListener('click', e => {
    Btn.classList.toggle('execute');
    Btn.textContent = Btn.classList.contains('execute') ? `${offText}` : `${onText}`;
  });
}

function setupPairToggleButton(deleteId, changeId, onToggle) {
  const deleteBtn = document.getElementById(deleteId);
  const changeBtn = document.getElementById(changeId);

  if (!deleteBtn || !changeBtn) return;

  deleteBtn.addEventListener('click', () => {
    deleteBtn.classList.toggle('execute');
    deleteBtn.textContent = deleteBtn.classList.contains('execute') ? '삭제중' : '삭제';

    // 반대쪽은 항상 해제
    changeBtn.classList.remove('execute');
    changeBtn.textContent = '수정';

    onToggle();
  });

  changeBtn.addEventListener('click', () => {
    changeBtn.classList.toggle('execute');
    changeBtn.textContent = changeBtn.classList.contains('execute') ? '수정중' : '수정';

    deleteBtn.classList.remove('execute');
    deleteBtn.textContent = '삭제';

    onToggle();
  });
}


function setupModalOpenClose(openId, closeId, modalId) {
  const openBtn = document.getElementById(openId);
  const closeBtn = document.getElementById(closeId);
  const modal = document.getElementById(modalId);

  if (openBtn) openBtn.addEventListener('click', () => {
    modal.dataset.id = '0';
    modal.classList.remove('hidden');
    document.body.classList.add('overflow-hidden');
    modalScroll('top-scroll', modalId, 'top');
    modalScroll('bottom-scroll', modalId, 'bottom');
  });

  if (closeBtn) closeBtn.addEventListener('click', () => {
    modal.classList.add('hidden');
    document.body.classList.remove('overflow-hidden');
    pageScroll('top-scroll', 'top');
    pageScroll('bottom-scroll', 'bottom');
  });
}

function setupSearchForm(dto, formId, sortSelectId, targetTypes, renderFn) {
  const form = document.getElementById(formId);
  const sortSelect = document.getElementById(sortSelectId);

  if (!form || !sortSelect) return;

  form.addEventListener('submit', async e => {
    e.preventDefault();
    const keyword = e.target.input.value;
    const sort = sortSelect.value;

    if (targetTypes?.length) {
      for (let type of targetTypes) {
        const res = await fetch(`/api/${dto}/search?keyword=${encodeURIComponent(keyword)}&type=${type}&sort=${sort}&direction=asc`);
        const filtered = await res.json();
        renderFn(type, filtered);
      }
    } else {
      const res = await fetch(`/api/${dto}/search?keyword=${encodeURIComponent(keyword)}&sort=${sort}&direction=asc`);
      const filtered = await res.json();
      renderFn(filtered);
    }
  });
}

function resetButton(dto, resetId, sortSelectId, targetTypes, renderFn) {
  const reset = document.getElementById(resetId);
  const sortSelect = document.getElementById(sortSelectId);

  if (!reset || !sortSelect) return;

  reset.addEventListener('click', async e => {
    e.preventDefault();
    const sort = sortSelect.value;

    if (targetTypes?.length) {
      for (let type of targetTypes) {
       if (type === 'trialDesign'||type === 'designFeature'){
         const res = await fetch(`/api/${dto}/tier?type=${type}`);
         const filtered = await res.json();
         renderFn(type, filtered);
       }
       else {
         const res = await fetch(`/api/${dto}?type=${type}&sort=${sort}&direction=asc`);
         const filtered = await res.json();
         renderFn(type, filtered);
       }
      }
    } else {
      const res = await fetch(`/api/${dto}?sort=${sort}&direction=asc`);
      const filtered = await res.json();
      renderFn(filtered);
    }
  });
}

function hideHoverButton(btnId, boxId) {
  const button = document.getElementById(btnId);
  const box = document.getElementById(boxId);
      if (!button || !box) {
        return; // 요소 못 찾음 → break 시그널
      }
  function show() {
    box.classList.remove('hidden');
  }

  function hide() {
    box.classList.add('hidden');
  }

  button.addEventListener('mouseenter', show);
  box.addEventListener('mouseenter', show);

  button.addEventListener('mouseleave', () => {
    setTimeout(() => {
      if (!box.matches(':hover')) hide();
    }, 100);
  });

  box.addEventListener('mouseleave', hide);
}

function hideClickButton(btnId, boxId){
    const button = document.getElementById(btnId);
    const hidden = document.getElementById(boxId);
      if (!button || !hidden) {
        return false; // 요소 못 찾음 → break 시그널
      }
    button.addEventListener('click', e=>{
       hidden.classList.toggle('hidden');
       button.classList.toggle('text-gray-500');
    });
    return true;
}

function scroll(targetId, behavior = 'smooth', offset = 70) {
  const el = document.getElementById(targetId);
  if (!el) return;

  const top = el.getBoundingClientRect().top + window.scrollY - offset;

  window.scrollTo({
    top,
    behavior,
  });
}

function indexScroll(indexId, type, behavior = 'smooth', offset = 70) {
  document.querySelectorAll(`#${indexId} ${type}`).forEach(item => {
    item.addEventListener('click', () => {
      const targetId = item.getAttribute('data-target');
      if (!targetId) return;

      scroll(targetId, behavior, offset);
    });
  });
}


function pageScroll(btnId, direction = 'top', behavior = 'smooth') {
  resetEventListener(btnId);
  const button = document.getElementById(btnId);
  if (!button) return;

  button.addEventListener('click', () => {
    const y = direction === 'top' ? 0 : document.body.scrollHeight;
    window.scrollTo({ top: y, behavior });
  });
}

function modalScroll(btnId, modalId, direction = 'top', behavior = 'smooth') {
  resetEventListener(btnId);
  const button = document.getElementById(btnId);
  const modal = document.getElementById(modalId);
  if (!button || !modal) return;

  // modal 자체는 fixed라 스크롤 안먹힘
  const scrollContainer = modal.querySelector('.modal-content');
  if (!scrollContainer) return;

  button.addEventListener('click', () => {
    const y = direction === 'top' ? 0 : scrollContainer.scrollHeight;
    scrollContainer.scrollTo({ top: y, behavior });
  });
}



function noteLink(id, rid){
   const link = document.getElementById(id);
   const rlink = document.getElementById(rid);
   if(!link||!rlink){
     return false;
   }
   createTooltip(id,rlink.innerHTML);
   return true;
}

function onlyOneCheckboxes(checkboxIds) {
  // 모든 체크박스를 하나의 배열로 통합
  const boxes = checkboxIds.flatMap(id =>
    Array.from(document.querySelectorAll(`#${id}-checkboxes input[type="checkbox"]`))
  );

  boxes.forEach(box => {
    box.addEventListener('click', () => {
      if (box.checked) {
        boxes.forEach(b => {
          if (b !== box) b.checked = false;
        });
      }
    });
  });
}

function selectList(types, filterFn) {
  types.forEach(type => {
    const tagList = document.getElementById(`${type}-list`);
    if (!tagList || tagList.dataset.listenerAttached) return;

    tagList.addEventListener('click', async e => {
      if (e.target.tagName === 'LI') {
        e.target.classList.toggle('selected');
        filterFn();
      }
    });

    tagList.dataset.listenerAttached = "true";
  });
}

function selectChange(boxId){
    document.getElementById(boxId).addEventListener('change', (e) => {
      if (e.target.tagName === 'SELECT') {
        const selected = e.target.options[e.target.selectedIndex];
        e.target.className = selected.className;
      }
    });
}

function journalEvent(journalMap, loadJournals){
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
               renderEffectCache(item);
            }
            modal.classList.remove('hidden');
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
}
function supplementEvent(supplementMap, loadSupplements) {
   document.getElementById('supplement-body').addEventListener('click', async e => {
       const row = e.target.closest('tr');
       const itemId = Number(row?.dataset.id);
       const item = journalMap.get(itemId);
       const modal = document.getElementById('modal');
       const modalId = modal?.dataset.id;
       if (!row||!itemId) {
           return;
       }
       // 삭제 모드
       if (document.getElementById('toggle-delete') ?.classList.contains('execute')) {
           e.preventDefault(); // 링크 이동 방지
           if (confirm(`'${item.korName}' 을 삭제할까요?`)) {
              await fetch(`/api/supplements/${itemId}`, {
              method: 'DELETE'
              });
              await loadSupplements();
           }
            return;
       }

       // 저장 버튼
       if (e.target.classList.contains('save-btn')) {
           const types = ArrayCheckboxesById('type');
           const updated = {
                 id: itemId,
                 korName: row.querySelector('[name="korName"]').value,
                 engName: row.querySelector('[name="engName"]').value,
                 dosage: row.querySelector('[name="dosage"]').value,
                 cost: parseFloat(row.querySelector('[name="cost"]').value),
                 types
           };

           const res = await fetch(`/api/supplements/${item.id}`, {
                 method: 'PUT',
                 headers: {
                 'Content-Type': 'application/json'
                 },
                 body: JSON.stringify(updated)
           });

           if (res.ok) {
                 alert('저장되었습니다');
                 await loadSupplements();
                 document.getElementById('list-sort')?.dispatchEvent(new Event('change'));
           } else {
                 alert('저장 실패');
           }
       }
       // 태그 모달
       if (e.target.classList.contains('modal-btn')) {
           //checkCheckboxesById('type', e.taget.types);
           document.getElementById('modal').classList.remove('hidden');
       }
   });
}

function themeSelect(selectId, iconId) {
  const select = document.getElementById(selectId);
  const icon = document.getElementById(iconId);
  function applyTheme(theme) {
      for (const c of [...document.documentElement.classList]) {
        if (c.startsWith('theme-')) {
          document.documentElement.classList.remove(c);
        }
      }
      document.documentElement.classList.add(`theme-${theme}`);
      icon.src = `/image/icon-${theme}.png`;
      select.value = theme;
  }
  select.addEventListener('change', e => {
    const selected = e.target.value;
    applyTheme(selected);
    localStorage.setItem('selectedTheme', selected);
  });
}

export {
  setupFoldToggle,
  setupSortTrigger,
  setupToggleButton,
  setupPairToggleButton,
  setupModalOpenClose,
  setupSearchForm,
  resetButton,
  hideHoverButton,
  hideClickButton,
  indexScroll,
  pageScroll,
  modalScroll,
  noteLink,
  onlyOneCheckboxes,
  selectList,
  selectChange,
  journalEvent,
  supplementEvent,
  themeSelect
};
