import{
  createA,
  createProduct,
  createAlarm,
  createNoticeSvg,
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
  const checkboxes = document.querySelectorAll(`#${type}-checkboxes input`);

  // ✅ 태그가 없거나 빈 값이면 전체 체크 해제
  if (!tagList || (Array.isArray(tagList) && tagList.length === 0)) {
    checkboxes.forEach(cb => cb.checked = false);
    return;
  }

  const tags = Array.isArray(tagList) ? tagList : [tagList];
  const tagSet = new Set(tags.map(tag => String(tag.id)));

  checkboxes.forEach(cb => {
    cb.checked = tagSet.has(cb.value);
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




function getCookie(name) {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

function deleteCookie(name) {
  document.cookie = `${name}=; Max-Age=0; path=/`;
}

async function checkLogin() {
  const loginBtn = document.getElementById('user-dropdown-toggle');
  const userDiv = document.getElementById("user-info");
  const bookmark = document.getElementById("bookmark");
  const cart = document.getElementById("cart");
  const totalPrice = document.getElementById("total-price");
  const alarm = document.getElementById("alarm");
  const alarmSvg = document.getElementById('alarm-svg');

  async function fetchUser() {
    const res = await fetch("/api/user/me", { credentials: "include" });

    if (res.ok) {
      console.log("사용자 정보 불러옴.");
      loginBtn.textContent = "마이페이지";
      const data = await res.json();
      userDiv.textContent = `${data.username}`;
      document.body.dataset.level = `${data.level}`;
      bookmark.innerHTML = "";
      cart.innerHTML = "";
      totalPrice.innerHTML = `${Number(data.totalPrice).toLocaleString()}원`;
      alarm.innerHTML = "";
      alarmSvg.innerHTML = "";

      data.pages.forEach(p => {
        const link = createA(p.link, p.title, "logo");
        link.dataset.id = p.id;
        bookmark.appendChild(link);
      });

      data.products.forEach(p => {
        cart.appendChild(createProduct(p));
      });

      let unRead=false;
      data.alarms.forEach(a => {
        if(!a.isRead){
          unRead = true;
        }
        alarm.appendChild(createAlarm(a.alarm, a.isRead, checkLogin));
      })

      if(unRead){
        alarmSvg.appendChild(createNoticeSvg());

        const myPageSvg = document.createElement('div');
        myPageSvg.classList.add('relative');
        myPageSvg.appendChild(createNoticeSvg());

        loginBtn.appendChild(myPageSvg);
      }

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

async function receiveData(baseUrl, params) {
  const url = `${baseUrl}?${params.toString()}`;
  const res = await fetch(url);
  if (!res.ok) {
    console.error("❌ 데이터 로딩 실패:", await res.text());
    return null;
  }
  return await res.json();
}

function insertData(response, renderFn, fnField = []) {
  if (!response) return;

  if (typeof response === 'object' && !Array.isArray(response)) {
    for (const [type, list] of Object.entries(response)) {
      renderFn(type, list, ...fnField);
    }
  } else {
    renderFn(response, ...fnField);
  }
}

function showLoadBtn(hasMore='true', loadId = 'load-more') {
  const btn = document.getElementById(loadId);
  if (!btn) return;

  if (hasMore) {
    btn.classList.remove('hidden');
  } else {
    btn.classList.add('hidden');
  }
}

export {
  switchTagList,
  checkCheckboxes,
  ArrayCheckboxesById,
  ArrayCheckboxesByName,
  resetModal,
  resetEventListener,
  checkLogin,
  receiveData,
  insertData,
  showLoadBtn
};