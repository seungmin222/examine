import{
  hideClickButton,
    hideContentButton,
  hideHoverButton,
  pageScroll,
  noteLink,
  logout,
  setupToggleButton,
  setupPairToggleButton,
    addBookmark,
    deleteBookmark,
    updateScrollProgress,
    iherbCouponRefresh,
    updateCart,
    tooltipEvent,
    alarmEvent,
    readAllAlarm,
    deleteAllAlarm,
    receiveAlarm,
    cartEvent,
    alarmBoxEvent
} from './event.js';

import{
    createNumberSVG,
    createPathWithParam,
    createCopyIcon,
    createIherbCoupon,
} from './create.js';

import{
    checkLogin,
    resetEventListener,
} from './utils.js';
import{
    renderButton
} from './render.js';

async function loadNavEvent(){
    const userLink = document.getElementById("user");
    userLink.href=`/user/login?redirect=${encodeURIComponent(window.location.href)}`;
    if (await checkLogin()){
        userLink.removeAttribute("href"); // 링크 제거
        userLink.addEventListener("click", e => e.preventDefault());

        await setupToggleButton('bookmark-delete-toggle', checkLogin, '삭제','삭제중');
        alarmEvent('alarm', checkLogin);
        readAllAlarm('alarm-read', checkLogin);
        deleteAllAlarm('alarm-delete', checkLogin);
        receiveAlarm(checkLogin);

        resetEventListener('bookmark');
        await deleteBookmark('bookmark','bookmark-delete-toggle',checkLogin); //이벤트 리스너 초기화
        await addBookmark('bookmark-save',checkLogin);

        resetEventListener('cart');
        cartEvent('cart', checkLogin);
        updateCart('cart', checkLogin);
        logout('logout');
        if(document.body.dataset.level>=10){
            iherbCouponRefresh('iherb-dropdown-toggle');
        }
    }

    themeSelect('theme','icon');
    alarmBoxEvent();
    await loadIherbCoupon();
}

async function loadPage() {
    const base = createPathWithParam();
    const res = await fetch(`/api/pages/current?link=/${encodeURIComponent(base)}`)

    if (res.ok) {
        const id = await res.text();
        document.getElementById('bookmark').dataset.id = id;
    } else {
        console.error("페이지 ID를 불러오지 못했습니다");
    }
}

function renderIndex(){
  const index = document.getElementById('index');
  index.innerHTML = ``;
    for (let i = 1; ; i++) {
      const num = document.getElementById(`index-${i}`);
      if (!num) {
          console.log(`${i-1}까지 목차 연결`)
          break;
      } // 요소를 못 찾으면 중단
      const link = document.createElement('a');
      link.id = `r-index-${i}`;
      link.href = `#index-${i}`;
      link.classList.add('index');
      const icon = createNumberSVG(i);
      link.appendChild(icon);
      link.append(num.textContent);
      index.appendChild(link);
    }
}

async function loadScroll(){
  await fetch('/module/scroll-button.html')
  .then(res => res.text())
  .then(html => {
   document.getElementById('scroll-button').insertAdjacentHTML('beforeend', html);

  });
  pageScroll('top-scroll', 'top');
  pageScroll('bottom-scroll', 'bottom');
  updateScrollProgress();
}
function loadScrollEvent(){
    pageScroll('top-scroll', 'top');
    pageScroll('bottom-scroll', 'bottom');
    updateScrollProgress();
}

async function loadModalController(){
    await fetch('/module/controller/modal-controller.html')
      .then(res => res.text())
      .then(html => {
        const remote = document.getElementById('modal-controller');
        if (!remote) return
        remote.innerHTML = html;
      });
}

async function loadTagController(){
    await fetch('/module/controller/tag-controller.html')
      .then(res => res.text())
      .then(html => {
        const remote = document.getElementById('tag-controller');
        if (!remote) return
        remote.innerHTML = html;
      });
}

function loadFold(){
    for (let i = 1; ; i++) {
      const success = hideContentButton(`index-${i}`, `content-${i}`);
      if (!success) break; // 요소를 못 찾으면 중단
    }
}

function renderNote(){
    for (let i = 1; ; i++) {
      const success = noteLink(`inote-${i}`, `note-${i}`);
      if (!success) {
          console.log(`${i-1}까지 주석 연결`);
          break;
      } // 요소를 못 찾으면 중단
    }
}


async function loadLoginInfo() {
    const container = document.createElement("div");
    container.id = "user-info";
    container.className = "text-sm p-2";

    try {
        const res = await fetch("/api/user/me");
        if (!res.ok) throw new Error();

        const data = await res.json();
        container.textContent = `👤 ${data.username} 님`;
        container.classList.add("text-green-600");
    } catch (e) {
        container.textContent = "🔒 로그인 필요";
        container.classList.add("text-red-500");
    }

    document.body.prepend(container); // 페이지 맨 위에 붙이기 (원하면 다른 위치로 이동 가능)
}

async function loadButton(loadFn){
    const level = Number(document.body.dataset.level);
    console.log(level);
    if (level>=10){
        console.log("admin: 수정, 삭제 버튼 활성화");
        renderButton('button-box','toggle-change','수정','');
        renderButton('button-box','toggle-delete','삭제','');
        setupPairToggleButton(loadFn);
    }
    else if (level>=1){
        console.log("user: 수정 버튼 활성화");
        renderButton('button-box','toggle-change','수정','');
        setupToggleButton('toggle-change', loadFn, '수정','수정중');
    }
}

async function loadIherbCoupon() {
    const container = document.getElementById("iherb-coupon");
    if (!container) return;

    try {
        const res = await fetch("/api/sale/iherb");
        const data = await res.json();

        if(res.ok){
            console.log('쿠폰 정보 불러옴');
        }

        if (!Array.isArray(data) || data.length === 0) {
            container.innerHTML = "<li class='w-fit'>현재 진행 중인 할인 정보가 없습니다.</li>";
            return;
        }

        container.innerHTML = ""; // 기존 내용 비움

        data.forEach(coupon => {
            const li = createIherbCoupon(coupon);
            container.appendChild(li);
        });

    } catch (e) {
        console.error("iHerb 쿠폰 로딩 실패", e);
        container.innerHTML = "<li>오류가 발생했습니다.</li>";
    }
}



//메인 모듈 로딩 후에 실행
async function loadBasicModule(){
  await loadModalController(); //모달 리모컨
  await loadTagController(); //태그 리모컨
  await loadFold(); // 콘텐트 숨기기
  renderNote(); //주석 연결
  renderIndex();
  await loadPage();

}

async function loadBasicEvent(){//index 독립 이벤트만
    console.log("basicEvent");
    await loadNavEvent();
    loadScrollEvent();

}
export{
   loadBasicModule,loadBasicEvent,
   renderIndex, loadNavEvent,
    loadScrollEvent,
    loadButton,
};