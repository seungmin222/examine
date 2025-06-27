import {
  createPopper
} from 'https://unpkg.com/@popperjs/core@2/dist/esm/popper.js';

function createTagList(type, list) {
  const liList = list.map(tag => {
    const li = document.createElement('li');
    li.textContent = tag.name;
    li.dataset.id = tag.id;
    li.dataset.type = type;
    return li;
  });

  if (type === 'trialDesign') {
    liList.forEach((li, i) => {
      const tier = list[i]?.tier;
      if (tier) {
        li.classList.add(tier[0]);
      }
    });
  }

  return liList;
}

// tag 배열 받아서 한꺼번에 만들기
function createCheckbox(tag, prefix) {
  const cb = document.createElement('input');
  cb.type = 'checkbox';
  cb.value = tag.id;
  cb.id = `${prefix}-${tag.id}`;
  return cb;
}

function createLabelText(tag) {
  const span = document.createElement('span');
  span.textContent = tag.name;
  const tier = tag?.tier;
    if (tier) {
      span.classList.add(tier[0]);
    }
  return span;
}

function createTierSelectBox(tagId, prefix) {
  const gr = document.createElement('select');
  gr.name = `tier-${tagId}`;
  gr.id = `${prefix}-tier-${tagId}`;
  gr.className = 'modal';

  ['A', 'B', 'C', 'D', 'null'].forEach(g => {
    const opt = document.createElement('option');
    opt.value = g;
    opt.className = g;
    opt.textContent = g;
    if (g === 'null') opt.selected = true;
    gr.appendChild(opt);
  });

  gr.addEventListener('change', () => {
    gr.className = gr.value;
  });

  return gr;
}

function createModalInner(tag, type) {
  const wrapper = document.createElement('label');
  wrapper.classList.add('modal-inner');
  const cb = createCheckbox(tag, type);
  const labelText = createLabelText(tag);

  wrapper.append(cb, labelText);

  return wrapper;
}

//popper 라이브러리 사용
function createTooltip(anchorId, text, position = 'top', cls = '') {
  const anchor = document.getElementById(anchorId);
  if (!anchor) {
    console.warn(`Tooltip anchor not found: ${anchorId}`);
    return;
  }

  const tooltip = document.createElement('div');
  tooltip.className = 'tooltip-box hidden';
  tooltip.classList.add(cls);
  tooltip.innerHTML = `
    <div class="tooltip-body">${text}</div>
    <div class="tooltip-tail-wrapper" data-popper-arrow>
      <div class="tooltip-tail-border"></div>
      <div class="tooltip-tail"></div>
    </div>
  `;
  document.body.appendChild(tooltip);

  const popperInstance = createPopper(anchor, tooltip, {
    placement: position,
    modifiers: [
      {
        name: 'offset',
        options: { offset: [0, 10] },
      },
      {
        name: 'arrow',
        options: {
          element: tooltip.querySelector('[data-popper-arrow]'),
        },
      },
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


function checkCheckboxesById(type, tags = [], idField = 'id') {
  if(tags == null){
    return;
  }
  const tagArray = Array.isArray(tags) ? tags : [tags];  // 단일 객체도 배열로 포장
  const idSet = new Set(tagArray.map(e => e[idField]));

  document.querySelectorAll(`#${type}-checkboxes input`).forEach(cb => {
    cb.checked = idSet.has(+cb.value);
  });
}


function checkCheckboxes(type, tag) {
  document.querySelectorAll(`#${type}-checkboxes input`).forEach(cb => {
    cb.checked = (tag === cb.value);
  });
}


function ArrayCheckboxesById(type) {
    return Array.from(document.querySelectorAll(`#${type}-checkboxes input:checked`))
              .map(cb => (parseInt(cb.value) ));
}

function ObjectCheckboxesById(type) {
    const checked = document.querySelector(`#${type}-checkboxes input:checked`);
    return checked ? checked.value : null;
}

function ArrayCheckboxesByName(type) {
  return Array.from(document.querySelectorAll(`#${type}-checkboxes input:checked`))
    .map(cb => ({
      id: parseInt(cb.value),
      name: cb.nextElementSibling?.textContent.trim() ?? '(이름 없음)'
    }));
}

function resetModal(types){
   types.forEach(type => {
      const boxes = document.querySelectorAll(`#${type}-checkboxes input[type="checkbox"]`);
      boxes.forEach(box => box.checked = false);
    });
}

function resetEventListener(Id) {
  const oldElem = document.getElementById(Id);
  if (!oldElem) return;
  const newElem = oldElem.cloneNode(true); // true: 하위 요소까지 복제
  oldElem.parentNode.replaceChild(newElem, oldElem);
}

function renderEffectCache(item) {
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


function getCookie(name) {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

function deleteCookie(name) {
  document.cookie = `${name}=; Max-Age=0; path=/`;
}

async function checkLogin() {
  const userDiv = document.getElementById("user-info");
  const bookmark = document.getElementById("bookmark");

  async function fetchUser() {
    const res = await fetch("/api/user/me", { credentials: "include" });

    if (res.ok) {
      console.log("사용자 정보 불러옴.");
      const data = await res.json();
      userDiv.textContent = `${data.username}`;
      userDiv.dataset.level = `${data.level}`;
      bookmark.innerHTML = "";
      data.pages.forEach(page => {
        const link = document.createElement("a");
        link.href = page.link;
        link.textContent = page.title;
        link.dataset.id = page.id;
        link.rel = "noopener noreferrer";
        link.classList.add("logo");
        bookmark.appendChild(link);
      });
      return true;
    }

    // 401 → 엑세스 만료 → refresh 쿠키 있을 때만 재발급 시도
    const refreshToken = getCookie("refresh");
    if (res.status === 401 && refreshToken) {
      const refreshRes = await fetch("/api/redis/refresh", {
        method: "POST",
        credentials: "include"
      });

      if (refreshRes.ok) {
        return await fetchUser(); // 재요청
      } else {
        deleteCookie("refresh"); // 실패 → 쿠키 제거
        alert("세션이 만료되었습니다. 다시 로그인해주세요.");
      }
    }

    // 실패 처리
    userDiv.textContent = "";
    userDiv.dataset.level = "";
    bookmark.innerHTML = "";
    return false;
  }

  return await fetchUser();
}






export {
  createTierSelectBox,
  createModalInner,
  createTagList,
  checkCheckboxes,
  checkCheckboxesById,
  ArrayCheckboxesById,
  ObjectCheckboxesById,
  ArrayCheckboxesByName,
  resetModal,
  createTooltip,
  resetEventListener,
  renderEffectCache,
  checkLogin
};