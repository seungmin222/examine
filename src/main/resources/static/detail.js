import {
    loadBasic
} from '/util/load.js';


document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const supplementId = params.get('id');
    if (!supplementId) return;

    try {
        const res1 = await fetch(`/api/supplements/${supplementId}`);
        const res2 = await fetch(`/api/details/${supplementId}`);
        const res3 = await fetch(`/api/details/${supplementId}/pubmeds`);
        // 추후 논문리스트도 정렬, 검색, 폴드 등 추가
        const info = await res1.json();
        const detail = await res2.json();
        const pubmed = await res3.json();
        renderSupplement(info);
        renderDetail(detail);
        renderPubmed(pubmed);
        loadBasic();

    } catch (err) {
        document.getElementById('title').textContent = '성분을 불러올 수 없습니다';
        console.error(err);
    }
});

// ✏️ 수정 모드 토글
document.getElementById('toggle-change').addEventListener('click', async e => {
    e.target.classList.toggle('execute');
    const editMode = e.target.classList.contains('execute');
    e.target.textContent = editMode ? "수정중" : "수정";
    if (editMode) {
        enableEditMode();
    } else {
        disableEditMode();
    }

    const existingSaveBtn = document.getElementById('save-btn');
    if (existingSaveBtn) {
        existingSaveBtn.remove();
    } else {
        const saveBtn = document.createElement('button');
        saveBtn.id = 'save-btn';
        saveBtn.textContent = '저장';
        saveBtn.addEventListener('click', () => {
            saveDetail();
        });
        e.target.before(saveBtn); // 수정 버튼 뒤에 삽입
    }
});

function renderDetail(detail) {
    const intro = document.getElementById('intro');
    const positive = document.getElementById('positive');
    const negative = document.getElementById('negative');
    const mechanism = document.getElementById('mechanism');
    const dosage = document.getElementById('dosage');
    intro.innerHTML = `${detail.intro}`;
    positive.innerHTML = `${detail.positive}`;
    negative.innerHTML = `${detail.negative}`;
    mechanism.innerHTML = `${detail.mechanism}`;
    dosage.innerHTML = `${detail.dosage}`;
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
    const params = new URLSearchParams(window.location.search);
    const supplementId = params.get('id');
    const res = await fetch(`/api/details/${supplementId}`);
    const detail = await res.json();
    renderDetail(detail);
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
    const detail = await render.json();

    if (res.ok) {
        alert('✅ 수정 완료!');
        renderDetail(detail); // 서버로부터 다시 불러오기
    } else {
        alert('❌ 수정 실패');
        enableEditMode();
    }
}


function renderSupplement(supplement) {
    const title = document.getElementById('title');
    const name = document.getElementById('name');
    const ul = document.getElementById('supplement-info');

    title.textContent = `${supplement.korName}`;
    name.textContent = `${supplement.korName} (${supplement.engName})`;
    ul.innerHTML = '';

    const fields = [{
            label: '분류 ',
            value: supplement.types ? .map(t => t.name).join(', ') || '-'
        },
        {
            label: '복용량 ',
            value: supplement.dosage || '-'
        },
        {
            label: '비용 ',
            value: supplement.cost || '-'
        },
        {
            label: '기대효과 ',
            value: supplement.effects ? .map(e => e.effectTag ? .name).join(', ') || '-'
        },
        {
            label: '부작용 ',
            value: supplement.sideEffects ? .map(e => e.name).join(', ') || '-'
        },
        {
            label: '수정 시각 ',
            value: supplement.date || '-'
        }
    ];

    fields.forEach(f => {
        const li = document.createElement('li');
        li.innerHTML = `<strong>${f.label}:</strong> ${f.value}`;
        li.className = 'detail';
        ul.appendChild(li);
    });
}

function renderPubmed(pubmed) {

    const ul = document.getElementById('pubmed-list');
    ul.innerHTML = '';

    pubmed.forEach(e => {
        const li = document.createElement('li');
        li.innerHTML = `<a href="${e.link}" target="_blank" class="tooltip" data-tooltip="${e.summary}" >
    ${e.title}
    </a>`;
        ul.appendChild(li);
    });
}