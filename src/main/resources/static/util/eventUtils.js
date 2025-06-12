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
    modal.style.display = 'block';
  });

  if (closeBtn) closeBtn.addEventListener('click', () => {
    modal.style.display = 'none';
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
       button.classList.toggle('text-gray-300');
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
  const button = document.getElementById(btnId);
  if (!button) return;

  button.addEventListener('click', () => {
    const y = direction === 'top' ? 0 : document.body.scrollHeight;
    window.scrollTo({ top: y, behavior });
  });
}

function noteLink(id, rid){
   const link = document.getElementById(id);
   const rlink = document.getElementById(rid);
   if(!link||!rlink){
     return false;
   }
   link.dataset.tooltip = rlink.textContent;
   link.dataset.position = 'top';
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
  noteLink,
  onlyOneCheckboxes,
  selectList,
  selectChange
};
