ㅣㅐㅎimport {
  resetEventListener,
} from '/util/utils.js';
import{
  createTailTooltip,
    createNewAlarm
} from '/util/create.js';
import {
  createPopper
} from 'https://unpkg.com/@popperjs/core@2/dist/esm/popper.js';


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

  Btn.addEventListener('click', async e => {
    Btn.classList.toggle('execute');
    Btn.textContent = Btn.classList.contains('execute') ? `${offText}` : `${onText}`;
    await loadFn();
  });
}

function setupPairToggleButton( loadFn = () => {},
                                 btnId1 = 'toggle-change',
                                 btnId2 = 'toggle-delete',
                                 onText1 = '수정중',
                                 offText1 = '수정',
                                 onText2 = '삭제중',
                                 offText2 = '삭제'
                               ) {
  const btn1 = document.getElementById(btnId1);
  const btn2 = document.getElementById(btnId2);

  if (!btn1 || !btn2) return;

  btn1.addEventListener('click', async () => {
    const active = btn1.classList.toggle('execute');
    btn1.textContent = active ? onText1 : offText1;

    btn2.classList.remove('execute');
    btn2.textContent = offText2;

    await loadFn();
  });

  btn2.addEventListener('click', async () => {
    const active = btn2.classList.toggle('execute');
    btn2.textContent = active ? onText2 : offText2;

    btn1.classList.remove('execute');
    btn1.textContent = offText1;

    await loadFn();
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

function setupSearchForm(baseUrl, formId, sortSelectId, targetTypes, renderFn, renderField = []) {
  const form = document.getElementById(formId);
  const sortSelect = document.getElementById(sortSelectId);

  if (!form || !sortSelect) return;

  form.addEventListener('submit', async e => {
    e.preventDefault();

    const keyword = e.target.input.value;
    const sort = sortSelect.value;

    const params = new URLSearchParams({
      keyword: keyword,
      sort: sort,
      direction: 'asc'
    });

    if (targetTypes?.length) {
      params.append("type", targetTypes.join(","));
    }

    const url = `${baseUrl}?${params.toString()}`;
    const res = await fetch(url);

    if (!res.ok) {
      console.error("❌ 검색 실패:", await res.text());
      return;
    }

    const data = await res.json();

    if (data && typeof data === 'object' && !Array.isArray(data)) {
      // 👉 Map 형식 (객체)
      for (const [type, list] of Object.entries(data)) {
        renderFn(type, list, ...renderField);
      }
    } else {
      // 👉 단일 리스트 (배열)
      renderFn(data, ...renderField);
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
   createTailTooltip(id,rlink.innerHTML);
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

function applyTheme(theme, icon) {
  for (const c of [...document.documentElement.classList]) {
    if (c.startsWith('theme-')) {
      document.documentElement.classList.remove(c);
    }
  }
  document.documentElement.classList.add(`theme-${theme}`);
  icon.src = `/image/icon-${theme}.png`;
}

function themeSelect(ulId, iconId) {
  const ul = document.getElementById(ulId);
  const icon = document.getElementById(iconId);
  if (!ul||!icon) {
    console.warn(`❗ 테마 선택 ul 요소가 없습니다: #${ulId}`);
    return;
  }

  ul.addEventListener('click', (e) => {
    const li = e.target.closest('li[data-theme]');
    if (!li || !ul.contains(li)) return;

    const theme = li.dataset.theme;
    if (theme) {
      applyTheme(theme, icon);
    }
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
        createNewAlarm("북마크 추가 완료!");
        await loadFn();
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
    const box = document.getElementById(boxId);
    const deleteButton = document.getElementById(deleteId);

    if (!box || !deleteButton) {
      console.warn("해당 요소가 존재하지 않습니다.");
      return;
    }

    const deleteMode = deleteButton.classList.contains('execute');

    box.addEventListener('click', async (e) => {

      if (!deleteMode) {
        return;
      }
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
          createNewAlarm("북마크 삭제 완료");
          // 선택적으로 DOM에서 항목 제거
          anchor.remove();
          await loadFn();
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

function updateCart(cartId = 'cart', loadFn = () => {}) {
  const cart = document.getElementById(cartId);
  if (!cart) return;

  cart.addEventListener("input", async (e) => {
    const target = e.target;
    const li = target.closest("li");
    if (!li || !li.dataset.id) return;

    const productId = li.dataset.id;
    const quantityInput = li.querySelector('[name="quantity"]');
    const checkBox = li.querySelector('[name="isChecked"]');
    if (!quantityInput || !checkBox) return;

    // 수량 변경일 경우에만 처리
    if (target.name === "quantity") {
      await sendCartUpdate(productId, quantityInput.value, checkBox.checked, loadFn);
    }
  });

  cart.addEventListener("change", async (e) => {
    const target = e.target;
    const li = target.closest("li");
    if (!li || !li.dataset.id) return;

    const productId = li.dataset.id;
    const quantityInput = li.querySelector('[name="quantity"]');
    const checkBox = li.querySelector('[name="isChecked"]');
    if (!quantityInput || !checkBox) return;

    // 체크박스 변경일 경우에만 처리
    if (target.name === "isChecked") {
      await sendCartUpdate(productId, quantityInput.value, checkBox.checked, loadFn);
    }
  });
}


// 실제 API 호출 함수
async function sendCartUpdate(productId, quantity, isChecked, loadFn=()=>{}) {
  const data = {
    id: productId,
    quantity,
    isChecked
  };

  const res = await fetch("/api/user/cart", {
    method: "PUT",
    credentials: "include",
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify([data])  // 백엔드가 배열 받는 구조라면 그대로 유지
  });

  if (res.ok) {
    console.log("✅ 장바구니 업데이트 완료", data);
    await loadFn();
  } else {
    console.warn("❌ 장바구니 업데이트 실패", await res.text());
  }
}

function cartEvent(containerId='cart', loadFn = () => {}) {
  const container = document.getElementById(containerId);
  if (!container) return;

  container.addEventListener('click', async (e) => {
    const target = e.target;
    const li = target.closest('li');
    if (!li || !target.name) return;

    const cartId = li.dataset.id;
    if (!cartId) return;

    if (target.name === 'delete') {
      const res = await fetch(`/api/user/cart/${cartId}`, {
        method: 'DELETE' ,
        credentials: "include"
      });
      if (res.ok) {
        console.log(`🗑️ 장바구니 삭제: ${cartId}`);
        await loadFn();
      } else {
        console.error(await res.text());
      }
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

function iherbCouponRefresh(buttonId = "iherb-dropdown-toggle") {
  const button = document.getElementById(buttonId);
  if (!button) return;

  button.addEventListener("click", async () => {
    try {
      const res = await fetch("/api/sale/iherb/refresh", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify([])
      });

      if (!res.ok) throw new Error("요청 실패");

      const result = await res.text();
      alert("✅ 쿠폰 새로고침 완료: " + result);
    } catch (e) {
      console.error("❌ 쿠폰 새로고침 실패", e);
      alert("오류가 발생했습니다.");
    }
  });
}

function tooltipEvent(anchorId, tooltipId, position = 'top') {
  const anchor = document.getElementById(anchorId);
  const tooltip = document.getElementById(tooltipId);
  if (!anchor || !tooltip) {
    console.warn(`Anchor or tooltip not found: ${anchorId}`);
    return;
  }

  const popperInstance = createPopper(anchor, tooltip, {
    placement: position,
    modifiers: [
      {
        name: 'preventOverflow',
        options: {
          boundary: 'viewport',
        },
      },
    ],
  });

  let hideTimer = null;

  const show = () => {
    clearTimeout(hideTimer);
    tooltip.classList.remove('hidden');
    popperInstance.update();
  };

  const hide = () => {
    hideTimer = setTimeout(() => {
      tooltip.classList.add('hidden');
    }, 100);
  };

  anchor.addEventListener('mouseenter', show);
  anchor.addEventListener('mouseleave', hide);
  tooltip.addEventListener('mouseenter', show);
  tooltip.addEventListener('mouseleave', hide);
}

function deleteAlarm(containerId='alarm', loadFn = () => {}) {
  const container = document.getElementById(containerId);
  if (!container) return;

  container.addEventListener('click', async (e) => {
    const target = e.target;
    const li = target.closest('li');
    if (!li || !target.name) return;

    const alarmId = li.dataset.id;
    if (!alarmId) return;

    if (target.name === 'read') {
      const res = await fetch(`/api/user/alarm/${alarmId}`, { method: 'PUT' });
      if (res.ok) {
        console.log(`✅ 알림 읽음 처리: ${alarmId}`);
        await loadFn();
      } else {
        console.error(await res.text());
      }
    }

    if (target.name === 'delete') {
      const res = await fetch(`/api/user/alarm/${alarmId}`, { method: 'DELETE' });
      if (res.ok) {
        console.log(`🗑️ 알림 삭제: ${alarmId}`);
        await loadFn();
      } else {
        console.error(await res.text());
      }
    }
  });
}

function alarmEvent(containerId='alarm', loadFn = () => {}) {
  const container = document.getElementById(containerId);
  if (!container) return;

  container.addEventListener('click', async (e) => {
    const target = e.target;
    const li = target.closest('li');
    if (!li || !target.name) return;

    const alarmId = li.dataset.id;
    if (!alarmId) return;

    // if (target.name === 'read') {
    //   const res = await fetch(`/api/user/alarm/${alarmId}`, { method: 'PUT' });
    //   if (res.ok) {
    //     console.log(`✅ 알림 읽음 처리: ${alarmId}`);
    //     loadFn();
    //   } else {
    //     console.error(await res.text());
    //   }
    // }

    if (target.name === 'delete') {
      const res = await fetch(`/api/user/alarm/${alarmId}`, { method: 'DELETE' });
      if (res.ok) {
        console.log(`🗑️ 알림 삭제: ${alarmId}`);
        await loadFn();
      } else {
        console.error(await res.text());
      }
    }
  });
}

function readAllAlarm(btnId='alarm-read', loadFn = () => {}) {
  const btn = document.getElementById(btnId);
  if (!btn) return;
  btn.addEventListener('click', async (e) => {
    e.preventDefault();
    const res = await fetch(`/api/user/alarm/readAll`, { method: 'PUT' });
    if (res.ok) {
      console.log(`🗑️ 알림 전부 읽음`);
      await loadFn();
    } else {
      console.error(await res.text());
    }
  })
}

function deleteAllAlarm(btnId='alarm-delete', loadFn = () => {}) {
   const deleteBtn = document.getElementById(btnId);
   if (!deleteBtn) return;

   deleteBtn.addEventListener('click', async (e) => {
     const res = await fetch(`/api/user/alarm/deleteAll`, { method: 'DELETE' });
     if (res.ok) {
       console.log(`🗑️ 알림 전부 삭제`);
       await loadFn();
     } else {
       console.error(await res.text());
     }
   })
}

function receiveAlarm(checkLoginFn=()=>{}) {
  const eventSource = new EventSource('/api/redis/alarm/subscribe');

  eventSource.addEventListener("alarm", async () => {
    await checkLoginFn();
    createNewAlarm("새 알림이 도착했습니다.");
  });

  eventSource.onerror = (err) => {
    console.error("🔌 SSE 연결 오류", err);
    eventSource.close();
  };
}

function alarmBoxEvent(boxId="alarm-container"){
  const box = document.getElementById(boxId);
  if (!box) {
    console.error('알림 박스를 찾을 수 없습니다');
    return;
  }
  box.addEventListener("click", (e) => {
    const toast = e.target.closest(".alarm-toast");
    if (toast) toast.remove();
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
   // deleteCart,

  updateScrollProgress,
  sidebarToggle,
  iherbCouponRefresh,
  updateCart,
    tooltipEvent,
    alarmEvent,
  readAllAlarm,
  deleteAllAlarm,
  receiveAlarm,
    cartEvent,
  alarmBoxEvent,
};
