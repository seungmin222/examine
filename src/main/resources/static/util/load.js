import{
  hideClickButton,
  indexScroll,
  hideHoverButton,
  pageScroll,
  noteLink,
  themeSelect
} from '/util/eventUtils.js';


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


async function loadBasic(){
  await loadNav(); // 네비게이션바
  loadTheme('selectedTheme','icon','theme-select'); // 테마
  await loadIndexNav(); // 사이드바
  await loadScroll(); // 위아래 스크롤 버튼
  await loadModalController(); //모달 리모컨
  await loadTagController(); //태그 리모컨
  await loadFold(); // 콘텐트 숨기기
  renderNote(); //주석 연결
}
export{
   loadBasic,
   renderIndex
};