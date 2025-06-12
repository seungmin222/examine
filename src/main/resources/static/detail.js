import {
    loadBasic
} from '/util/load.js';

import {
    renderSupplements,
    renderJournals,
    renderDetails
} from '/util/render.js';


document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const supplementId = params.get('id');
    if (!supplementId) {
        return;
    }
    try {
        loadSupplements(supplementId);
        loadDetails(supplementId);
        loadJournals(supplementId);
        loadBasic();
    } catch (err) {
        document.getElementById('title').textContent = '성분을 불러올 수 없습니다';
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
    const params = new URLSearchParams(window.location.search);
    const supplementId = params.get('id');
    const res = await fetch(`/api/details/${supplementId}`);
    const details = await res.json();
    renderDetails(details);
}

// 🟣 수정 완료: 내용 수집 후 저장 요청
async function saveDetail() {
    const params = new URLSearchParams(window.location.search);
    const supplementId = params.get('id');
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

async function loadJournals(supplementId){
    const res = await fetch(`/api/details/${supplementId}/journals`);
    const journals = await res.json();
    renderJournals(journals);
}

async function loadSupplements(supplementId){
    const res = await fetch(`/api/supplements/${supplementId}`);
    const supplements = await res.json();
    renderSupplements(supplements);
}

async function loadDetails(supplementId){
    const res = await fetch(`/api/details/${supplementId}`);
    const details = await res.json();
    renderDetails(details);
}