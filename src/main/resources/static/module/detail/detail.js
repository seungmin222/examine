import {
    loadButton
} from '/util/load.js';

import {
    journalEvent,
    supplementEvent
} from '/util/tableEvent.js';


import {
    renderSupplements,
    renderJournals,
    renderDetails,
    renderButton,
    renderTagTable
} from '/util/render.js';

const journalMap = new Map();
const supplementMap = new Map();


const params = new URLSearchParams(window.location.search);
const supplementId = params.get('id');

export async function init() {
    if (!supplementId) {
        return;
    }
    try {
        await loadButton(loadAll);
        await loadAll();
        supplementEvent(supplementMap,loadDetails);
        journalEvent(journalMap,loadJournals);

        document.getElementById('toggle-change').addEventListener('click',e=> {
            const editMode = e.target.classList.contains('execute');
            if(editMode) {
                enableEditMode();
            }
            else {
                disableEditMode();
            }
        });

        document.getElementById('save-button').addEventListener('click',e=>{
            const editMode = document.getElementById('toggle-change').classList.contains('execute');
            if(editMode) {
                saveDetail();
            }
        });

    } catch (err) {
        document.getElementById('title').textContent = '성분을 불러올 수 없습니다';
        console.error(err);
    }
}


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
    const sort = document.getElementById('list-sort').value;
    let dir = 'desc';
    if (sort === 'title'){
        dir = 'asc';
    }
    const res = await fetch(`/api/supplements/detail/${supplementId}/journals?sort=${sort}&direction=${dir}`);
    const journals = await res.json();
    renderJournals(journals, journalMap);
}

async function loadDetails(){
    const sort = document.getElementById('tag-sort').value;
    let dir = 'desc';
    if (sort === 'title'){
        dir = 'asc';
    }
    const res = await fetch(`/api/supplements/detail/${supplementId}?sort=${sort}&direction=${dir}`);
    const details = await res.json();
    document.title = details.supplement[0].engName;
    //const title = document.getElementById('title');
    //const subTitle = document.getElementById('index-1');
    //title.innerHTML = `${details.supplement[0].korName}`;
    //subTitle.innerHTML = `${details.supplement[0].korName}`;
    renderSupplements(details.supplement, supplementMap);
    renderDetails(details.detail);
    renderTagTable(details.effects, 'effect-body');
    renderTagTable(details.sideEffects, 'sideEffect-body');
}

async function loadAll(){
    loadDetails();
    loadJournals();
}