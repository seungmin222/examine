import{
  createA,
  createProduct
} from '/util/create.js';

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
  const cart = document.getElementById("cart");

  async function fetchUser() {
    const res = await fetch("/api/user/me", { credentials: "include" });

    if (res.ok) {
      console.log("사용자 정보 불러옴.");
      const data = await res.json();
      userDiv.textContent = `${data.username}`;
      document.body.dataset.level = `${data.level}`;
      bookmark.innerHTML = "";
      cart.innerHTML = "";

      data.pages.forEach(p => {
        const link = createA(p.link, p.title, "logo");
        link.dataset.id = p.id;
        bookmark.appendChild(link);
      });

      data.products.forEach(p => {
        cart.appendChild(createProduct(p));
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
  switchTagList,
  checkCheckboxes,
  ArrayCheckboxesById,
  ArrayCheckboxesByName,
  resetModal,
  resetEventListener,
  renderEffectCache,
  checkLogin,
};