import {
  createTooltip,
  resetEventListener,

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

function setupToggleButton(buttonId, loadFn = () => {}, onText='수정', offText='수정중') {
  resetEventListener(buttonId);
  const Btn = document.getElementById(buttonId);

  if (!Btn) return;

  Btn.addEventListener('click', e => {
    Btn.classList.toggle('execute');
    Btn.textContent = Btn.classList.contains('execute') ? `${offText}` : `${onText}`;
    loadFn();
  });
}

function setupPairToggleButton(deleteId, changeId, loadFn = () => {}) {
  const deleteBtn = document.getElementById(deleteId);
  const changeBtn = document.getElementById(changeId);

  if (!deleteBtn || !changeBtn) return;

  deleteBtn.addEventListener('click', () => {
    deleteBtn.classList.toggle('execute');
    deleteBtn.textContent = deleteBtn.classList.contains('execute') ? '삭제중' : '삭제';

    // 반대쪽은 항상 해제
    changeBtn.classList.remove('execute');
    changeBtn.textContent = '수정';

    loadFn();
  });

  changeBtn.addEventListener('click', () => {
    changeBtn.classList.toggle('execute');
    changeBtn.textContent = changeBtn.classList.contains('execute') ? '수정중' : '수정';

    deleteBtn.classList.remove('execute');
    deleteBtn.textContent = '삭제';

    loadFn();
  });
}


function setupModalOpenClose(openId, closeId, modalId) {
  const openBtn = document.getElementById(openId);
  const closeBtn = document.getElementById(closeId);
  const modal = document.getElementById(modalId);

  if (openBtn) openBtn.addEventListener('click', () => {
    modal.dataset.id = '';
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

function hideClickButton(btnId, boxId) {
  const button = document.getElementById(btnId);
  const box = document.getElementById(boxId);
  if (!button || !box) return;

  function show() {
    box.classList.remove('hidden');
  }

  function hide() {
    box.classList.add('hidden');
  }

  // 버튼 클릭 시 → 토글 (열고 닫기)
  button.addEventListener('click', (e) => {
    e.stopPropagation(); // 전파 방지 (닫힘 방지용)
    box.classList.toggle('hidden');
  });

  // 박스 클릭 시 → 전파 방지 (닫힘 방지용)
  box.addEventListener('click', (e) => {
    e.stopPropagation();
  });

  // 바깥 클릭 시 → 닫기
  document.addEventListener('click', () => {
    hide();
  });
}


function hideContentButton(btnId, boxId){
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

function updateScrollProgress() {
  const topCircle = document.querySelector('#top-scroll .scroll-circle');
  const bottomCircle = document.querySelector('#bottom-scroll .scroll-circle');

  if (!topCircle || !bottomCircle) return;

  const handleScroll = () => {
    const scrollTop = window.scrollY;
    const docHeight = document.documentElement.scrollHeight - window.innerHeight;
    const progress = docHeight === 0 ? 0 : scrollTop / docHeight;

    const dash = 283;
    topCircle.style.strokeDashoffset = dash * (1 - progress);
    bottomCircle.style.strokeDashoffset = dash * progress;
  };

  // 초기 적용 + 이벤트 바인딩
  handleScroll();
  window.addEventListener('scroll', handleScroll);
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

async function logout(buttonId){
    document.getElementById(buttonId).addEventListener('click', async () => {
        const res = await fetch("/api/logout", {
            method: "POST",
            credentials: "include" // 쿠키 포함 (세션 기반 인증 시 필수)
        });

        if (res.ok) {
            alert("로그아웃 성공");
            window.location.reload(); // 또는 원하는 페이지로 리다이렉트
        } else {
            const errText = await res.text();  // 오류 메세지 확인
            console.error("로그아웃 실패", errText);
            alert("로그아웃 실패");
        }
    });
}

async function addBookmark(buttonId, loadFn) {
  resetEventListener(buttonId);
  const button = document.getElementById(buttonId);
  if (!button) {
    console.warn(`북마크 버튼이 존재하지 않음: ${buttonId}`);
    return;
  }

  button.addEventListener('click', async (e) => {
    e.preventDefault();
    e.stopPropagation();

    const pageId = document.getElementById("bookmark").dataset.id;

    try {
      const res = await fetch(`/api/user/bookmark/${pageId}`, {
        method: "POST",
        credentials: "include" // 인증 쿠키 필요 시
      });

      if (res.ok) {
        alert("북마크 추가 완료!");
        loadFn();
      } else {
        const msg = await res.text();
        alert("실패: " + msg);
      }
    } catch (err) {
      console.error("요청 실패:", err);
      alert("에러 발생");
    }
  });
}


  async function deleteBookmark(boxId, deleteId, loadFn) {
    resetEventListener(boxId);
    const box = document.getElementById(boxId);
    const deleteButton = document.getElementById(deleteId);

    if (!box || !deleteButton) {
      console.warn("해당 요소가 존재하지 않습니다.");
      return;
    }

    const deleteMode = deleteButton.classList.contains('execute');

    if (!deleteMode) {
      return;
    }

    box.addEventListener('click', async (e) => {

      e.preventDefault();
      e.stopPropagation();

      const anchor = e.target.closest('a');
      if(!anchor){
        return;
      }
      else if (!anchor.dataset.id) {
        console.warn("페이지 ID가 존재하지 않음");
        return;
      }

      const pageId = anchor.dataset.id;

      try {
        const res = await fetch(`/api/user/bookmark/${pageId}`, {
          method: "DELETE",
          credentials: "include", // 로그인 세션 필요 시
        });

        if (res.ok) {
          alert("북마크 삭제 완료!");
          // 선택적으로 DOM에서 항목 제거
          anchor.remove();
          loadFn();
        } else {
          const msg = await res.text();
          alert("실패: " + msg);
        }
      } catch (err) {
        console.error("에러 발생:", err);
        alert("요청 중 오류 발생");
      }
    });
  }

function sidebarToggle(sidebarId, btnId) {
  const sidebar = document.getElementById(sidebarId);
  const toggleBtn = document.getElementById(btnId);

  if (!sidebar || !toggleBtn) {
    console.warn("Sidebar 또는 화살표 요소가 없습니다.");
    return;
  }

  toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('closed');
    toggleBtn.classList.toggle('rotate-180');
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
  hideContentButton,
  indexScroll,
  pageScroll,
  modalScroll,
  noteLink,
  onlyOneCheckboxes,
  selectList,
  selectChange,
  themeSelect,
  logout,
  addBookmark,
  deleteBookmark,
  updateScrollProgress,
  sidebarToggle
};
