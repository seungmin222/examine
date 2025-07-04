import {
    loadBasic,
    renderIndex
} from '/util/load.js';

import {
    journalEvent,
    supplementEvent
} from '/util/tableEvent.js';


import {
    renderSupplements,
    renderJournals,
    renderTagDetails,
    renderButton
} from '/util/render.js';

const journalMap = new Map();
const supplementMap = new Map();


const params = new URLSearchParams(window.location.search);
const id = params.get('id');
const type = params.get('type');

document.addEventListener('DOMContentLoaded', async () => {
    if (!id) {
        return;
    }
    try {
        await loadBasic(loadAll);
        await loadAll();
        supplementEvent(supplementMap,loadSupplements);
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
        document.getElementById('title').textContent = '태그를 불러올 수 없습니다';
        console.error(err);
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
    loadALl();
}

// 🟣 수정 완료: 내용 수집 후 저장 요청
async function saveDetail() {
    const data = {};
    document.querySelectorAll('.editable').forEach(el => {
        const key = el.dataset.field;
        data[key] = el.innerText.trim(); // 줄바꿈이 있다면 innerHTML로 바꿔도 됨
    });

    const res = await fetch(`/api/tag/detail/${type}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (res.ok) {
        alert('✅ 수정 완료!');
        loadALl(); // 서버로부터 다시 불러오기
    } else {
        alert('❌ 수정 실패');
        enableEditMode();
    }
}

async function loadJournals(){
    const res = await fetch(`/api/tags/detail/journals/${type}/${id}`);
    const journals = await res.json();
    renderJournals(journals, journalMap);
}

async function loadSupplements(){
    const res = await fetch(`/api/tags/detail/supplements/${type}/${id}`);
    const supplements = await res.json();
    renderSupplements(supplements, supplementMap);
}

async function loadTagDetails(){
    const res = await fetch(`/api/tags/detail/${type}/${id}`);
    const details = await res.json();
    renderTagDetails(details);
    const title = document.getElementById('title');
    const subTitle = document.getElementById('index-1');
}

async function loadAll(){
    loadSupplements();
    loadTagDetails();
    loadJournals();
}