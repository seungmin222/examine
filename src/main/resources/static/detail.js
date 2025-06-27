import {
    loadBasic,
    renderIndex
} from '/util/load.js';

import {
    setupPairToggleButton,
    journalEvent,
    supplementEvent
} from '/util/event.js';


import {
    renderSupplements,
    renderJournals,
    renderDetails,
    renderButton
} from '/util/render.js';

const params = new URLSearchParams(window.location.search);
const supplementId = params.get('id');

document.addEventListener('DOMContentLoaded', async () => {
    if (!supplementId) {
        return;
    }
    try {
        await loadAll();
        await loadBasic(loadAll);
        supplementEvent();
        journalEvent();


    } catch (err) {
        document.getElementById('title').textContent = '성분을 불러올 수 없습니다';
        console.error(err);
    }
});

document.getElementById('toggle-change').addEventListener('click',e=> {
    const editMode = e.target.classList.contains('execute');
    if(editMode) {
       disableEditMode();
    }
    else {
       enableEditMode();
    }
});

document.getElementById('save-button').addEventListener('click',e=>{
   const editMode = document.getElementById('toggle-change').classList.contains('execute');
   if(editMode) {
      saveDetail();
   }
});

// 🟢 수정모드 진입: p 태그 contenteditable 활성화 + 스타일 변경
function enableEditMode() {
    document.querySelectorAll('.editable').forEach(el => {
        el.setAttribute('contenteditable', 'true');
        el.classList.add('editing');
    });
}

async function disableEditMode() {
    document.querySelectorAll('.editable').forEach(el => {
        el.setAttribute('contenteditable', 'false');
        el.classList.remove('editing');
    });
    const res = await fetch(`/api/details/${supplementId}`);
    const details = await res.json();
    renderDetails(details);
}

// 🟣 수정 완료: 내용 수집 후 저장 요청
async function saveDetail() {
    const data = {};
    document.querySelectorAll('.editable').forEach(el => {
        const key = el.dataset.field;
        data[key] = el.innerText.trim(); // 줄바꿈이 있다면 innerHTML로 바꿔도 됨
    });

    const res = await fetch(`/api/details/${supplementId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    const render = await fetch(`/api/details/${supplementId}`);
    const details = await render.json();

    if (res.ok) {
        alert('✅ 수정 완료!');
        renderDetails(details); // 서버로부터 다시 불러오기
    } else {
        alert('❌ 수정 실패');
        enableEditMode();
    }
}

async function loadJournals(){
    const res = await fetch(`/api/details/${supplementId}/journals`);
    const journals = await res.json();
    const journalMap = new Map(); // 그냥 매개변수 채우는 용
    renderJournals(journals, journalMap);
}

async function loadSupplements(){
    const res = await fetch(`/api/supplements/${supplementId}`);
    const supplements = await res.json();
    renderSupplements(supplements);
    const title = document.getElementById('title');
    const subTitle = document.getElementById('index-1');
    title.innerHTML = `${supplements[0].korName}`;
    subTitle.innerHTML = `${supplements[0].korName}`;
    setTimeout(renderIndex, 0);
}

async function loadDetails(){
    const res = await fetch(`/api/details/${supplementId}`);
    const details = await res.json();
    renderDetails(details);
}

async function loadAll(){
    loadSupplements();
    loadDetails();
    loadJournals();
}