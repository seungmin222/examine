import{
  hideClickButton,
  indexScroll,
  hideHoverButton,
  pageScroll,
  noteLink,
  themeSelect,
  logout
} from '/util/event.js';

import{
    checkLogin
} from '/util/utils.js';
import{
    renderButton
} from '/util/render.js';

async function loadNav(){
  await fetch(`/module/nav.html`)
  .then(res => res.text())
  .then(html => {
  document.getElementById('load-basic').insertAdjacentHTML('beforeend', html);

    const script = document.createElement('script');
    script.type = 'module';
    script.src = '/module/module.js';
    document.body.appendChild(script);
  });
  if(await checkLogin()){
      const userLink = document.getElementById("user");
      userLink.removeAttribute("href"); // 링크 제거
      userLink.addEventListener("click", e => e.preventDefault());
      userLink.querySelector("#user-dropdown-toggle").textContent = "내 정보";
      hideHoverButton('user-dropdown-toggle','user-dropdown');
      hideHoverButton('user-info-dropdown-toggle','user-info-dropdown');
      hideHoverButton('bookmark-dropdown-toggle','bookmark-dropdown');
      hideHoverButton('memo-dropdown-toggle','memo-dropdown');
      logout('logout');
  }
  document.getElementById('user').href=`/user/login.html?redirect=${encodeURIComponent(window.location.href)}`;
  hideHoverButton('guide-dropdown-toggle','guide-dropdown');
  hideHoverButton('table-dropdown-toggle','table-dropdown');
  hideHoverButton('setting-dropdown-toggle','setting-dropdown');
  themeSelect('theme-select','icon');
}

async function loadIndexNav(){
  await fetch(`/module/index-nav.html`)
  .then(res => res.text())
  .then(html => {
  document.getElementById('load-basic').insertAdjacentHTML('beforeend', html);

    const script = document.createElement('script');
    script.type = 'module';
    script.src = '/module/module.js';
    document.body.appendChild(script);
  });
  renderIndex();
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
      link.textContent = num.textContent;
      index.appendChild(link);
    }
}

async function loadScroll(){
  await fetch('/module/scroll-button.html')
  .then(res => res.text())
  .then(html => {
   document.getElementById('load-basic').insertAdjacentHTML('beforeend', html);

    const script = document.createElement('script');
    script.type = 'module';
    script.src = '/module/module.js';
    document.body.appendChild(script);
  });
  pageScroll('top-scroll', 'top');
  pageScroll('bottom-scroll', 'bottom');
}

async function loadModalController(){
    await fetch('/module/modal-controller.html')
      .then(res => res.text())
      .then(html => {
        const remote = document.getElementById('modal-controller');
        if (!remote) return
        remote.innerHTML = html;
      });
}

async function loadTagController(){
    await fetch('/module/tag-controller.html')
      .then(res => res.text())
      .then(html => {
        const remote = document.getElementById('tag-controller');
        if (!remote) return
        remote.innerHTML = html;
      });
}

function loadFold(){
    for (let i = 1; ; i++) {
      const success = hideClickButton(`index-${i}`, `content-${i}`);
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

async function loadButton(){
    const level = Number(document.getElementById('user-info').dataset.level);
    if (level>=10){
        renderButton('button-box','toggle-delete','삭제','');
    }
    if (level>=1){
        renderButton('button-box','toggle-change','수정','');
    }
}


async function loadBasic(){
  await loadNav(); // 네비게이션바
  loadTheme('selectedTheme','icon','theme-select'); // 테마
  await loadIndexNav(); // 사이드바
  await loadScroll(); // 위아래 스크롤 버튼
  await loadModalController(); //모달 리모컨
  await loadTagController(); //태그 리모컨
  await loadFold(); // 콘텐트 숨기기
  renderNote(); //주석 연결
  await loadButton()
}
export{
   loadBasic,
   renderIndex
};