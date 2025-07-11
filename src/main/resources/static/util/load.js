import{
  hideClickButton,
    hideContentButton,
  hideHoverButton,
  pageScroll,
  noteLink,
  themeSelect,
  logout,
  setupToggleButton,
  setupPairToggleButton,
    addBookmark,
    deleteBookmark,
    updateScrollProgress,
} from '/util/event.js';

import{
    createNumberSVG,
    checkLogin
} from '/util/utils.js';
import{
    renderButton
} from '/util/render.js';

async function loadNavEvent(){
    const userLink = document.getElementById("user");
    userLink.href=`/user/login?redirect=${encodeURIComponent(window.location.href)}`;
    if (await checkLogin()){
        userLink.removeAttribute("href"); // 링크 제거
        userLink.addEventListener("click", e => e.preventDefault());
        userLink.querySelector("#user-dropdown-toggle").textContent = "내 정보";
        hideClickButton('user-dropdown-toggle','user-dropdown');
        hideHoverButton('alarm-dropdown-toggle','alarm-dropdown');
        hideHoverButton('user-info-dropdown-toggle','user-info-dropdown');
        hideHoverButton('bookmark-dropdown-toggle','bookmark-dropdown');
        hideHoverButton('memo-dropdown-toggle','memo-dropdown');
        await setupToggleButton('bookmark-delete-toggle', loadNavEvent, '삭제','삭제중');
        await deleteBookmark('bookmark','bookmark-delete-toggle',checkLogin);
        await addBookmark('bookmark-save',checkLogin);
        logout('logout');
    }
    hideHoverButton('guide-dropdown-toggle','guide-dropdown');
    hideHoverButton('table-dropdown-toggle','table-dropdown');
    hideClickButton('setting-dropdown-toggle','setting-dropdown');
    themeSelect('theme-select','icon');
}

async function loadPage() {
    const path = window.location.pathname;
    const res = await fetch(`/api/pages/current?link=${encodeURIComponent(path)}`)

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
      if (!num) break; // 요소를 못 찾으면 중단
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
      if (!success) break; // 요소를 못 찾으면 중단
    }
}

function loadTheme(localSave, iconId, selectId){
  const savedTheme = localStorage.getItem(localSave);
    if (savedTheme) {

      document.getElementById(iconId).src = `/image/icon-${savedTheme}.png`;
      document.getElementById(selectId).value = savedTheme;
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
    if (level>=10){
        renderButton('button-box','toggle-change','수정','');
        renderButton('button-box','toggle-delete','삭제','');
        setupPairToggleButton('toggle-delete', 'toggle-change', loadFn);
    }
    else if (level>=1){
        renderButton('button-box','toggle-change','수정','');
        setupToggleButton('toggle-change', loadFn, '수정','수정중');
    }
}



//메인 모듈 로딩 후에 실행
async function loadBasicModule(){
  await loadModalController(); //모달 리모컨
  await loadTagController(); //태그 리모컨
  await loadFold(); // 콘텐트 숨기기
  renderNote(); //주석 연결
    renderIndex();
}

async function loadBasicEvent(){//index 독립 이벤트만
    loadTheme('selectedTheme','icon','theme-select'); // 테마
    await loadNavEvent();
    loadScrollEvent();
    await loadPage();
}
export{
   loadBasicModule,loadBasicEvent,
   renderIndex, loadNavEvent,
    loadScrollEvent,
    loadButton
};