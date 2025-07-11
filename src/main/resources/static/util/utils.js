import {
  createPopper
} from 'https://unpkg.com/@popperjs/core@2/dist/esm/popper.js';

function createTagList(type, list) {
  const liList = list.map(tag => {
    const li = document.createElement('li');
    li.textContent = tag.korName;
    li.dataset.name = tag.engName;
    li.dataset.id = tag.id;
    li.dataset.type = type;
    li.classList.add(tag.tier[0]);
    return li;
  });
  return liList;
}

function switchTagList(type) {
  const list = document.getElementById(`${type}-list`);
  if (!list) return;

  // li 요소들 순회
  list.querySelectorAll('li').forEach(li => {
    const text = li.textContent;
    li.textContent = li.dataset.name;
    li.dataset.name = text;
  });
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
  span.textContent = tag.korName;
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
  if (cls) {
    tooltip.classList.add(cls);
  }
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

function checkCheckboxes(type, tagList) {
  const tagSet = new Set(tagList.map(tag => String(tag.id))); // ✅ 문자열 기준 Set 생성
  document.querySelectorAll(`#${type}-checkboxes input`).forEach(cb => {
    cb.checked = tagSet.has(cb.value); // ✅ cb.value도 string이니까 잘 작동
  });
}

function ArrayCheckboxesById(type) {
    return Array.from(document.querySelectorAll(`#${type}-checkboxes input:checked`))
              .map(cb => (parseInt(cb.value) ));
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

  renderCash(item.effects,'effect-cash');
  renderCash(item.sideEffects,'sideEffect-cash');

  function renderCash(list, cls){
    list?.forEach(effect => {
      const row = document.createElement('tr');
      row.classList.add(cls);
      row.dataset.supplementId = effect.supplementId;
      row.dataset.effectId = effect.effectId;

      const td1 = document.createElement('td');
      td1.textContent = effect.supplementKorName;

      const td2 = document.createElement('td');
      td2.textContent = effect.effectKorName;

      const td3 = document.createElement('td');
      const cohenD = document.createElement('input');
      cohenD.name = 'cohenD';
      cohenD.type = 'number';
      cohenD.classList.add('w-16');
      cohenD.value = effect.cohenD;
      cohenD.step = '0.1';
      cohenD.placeholder = "null";
      td3.appendChild(cohenD);

      const td4 = document.createElement('td');
      const pearsonR = document.createElement('input');
      pearsonR.name = 'pearsonR';
      pearsonR.type = 'number';
      pearsonR.classList.add('w-16');
      pearsonR.value = effect.pearsonR;
      pearsonR.step = '0.1';
      pearsonR.placeholder = "null";
      td4.appendChild(pearsonR);

      const td5 = document.createElement('td');
      const pValue = document.createElement('input');
      pValue.name = 'pValue';
      pValue.type = 'number';
      pValue.classList.add('w-24');
      pValue.value = effect.pValue;
      pValue.step = "0.001";
      pValue.placeholder = "null";
      td5.appendChild(pValue);

      row.appendChild(td1);
      row.appendChild(td2);
      row.appendChild(td3);
      row.appendChild(td4);
      row.appendChild(td5);

      container.appendChild(row);
    });
  }
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
      document.body.dataset.level = `${data.level}`;
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

function createNumberSVG(num) {
  const svgNS = "http://www.w3.org/2000/svg";
  const svg = document.createElementNS(svgNS, "svg");

  svg.setAttribute("width", "20");
  svg.setAttribute("height", "20");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("class", "number-icon");

  // 배경 원 (또는 사각형으로 바꿔도 됨)
  const rect = document.createElementNS(svgNS, "rect");
  rect.setAttribute("x", "2");           // 시작 x좌표
  rect.setAttribute("y", "2");           // 시작 y좌표
  rect.setAttribute("width", "20");      // 너비
  rect.setAttribute("height", "20");     // 높이
  rect.setAttribute("rx", "5");          // 둥근 모서리 반지름 (선택)
  rect.setAttribute("fill", "var(--color-button)");

  svg.appendChild(rect);

  // 숫자 텍스트
  const text = document.createElementNS(svgNS, "text");
  text.setAttribute("x", "12");
  text.setAttribute("y", "16");
  text.setAttribute("text-anchor", "middle");
  text.setAttribute("font-size", "12");
  text.setAttribute("fill", "white");
  text.setAttribute("font-weight", "bold");
  text.textContent = num;
  svg.appendChild(text);

  return svg;
}





export {
  createTierSelectBox,
  createModalInner,
  createTagList,
    switchTagList,
  checkCheckboxes,
  ArrayCheckboxesById,
  ArrayCheckboxesByName,
  resetModal,
  createTooltip,
  resetEventListener,
  renderEffectCache,
  checkLogin,
  createNumberSVG
};