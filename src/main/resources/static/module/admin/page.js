import {
    setupFoldToggle,
    setupSortTrigger,
    setupSearchForm,
    selectList
} from '/util/event.js';

import {
    renderPages
} from '/util/render.js';

import {
    loadButton
} from '/util/load.js';

import {
    pageEvent
} from '/util/tableEvent.js';


// 전역 맵
const pageMap = new Map();

// 초기 로딩
export async function init() {

    await loadButton(loadPages);
    await loadPages();
    // 테이블 클릭 이벤트리스너, 이벤트 위임이므로 한번만 추가
    pageEvent(pageMap, loadPages);

    // 접기 토글
    setupFoldToggle('toggle-fold', loadPages);

    // 정렬
    setupSortTrigger('list-sort', loadPages);

    setupSearchForm("pages", "search-form", "list-sort", null, renderPages);

    selectList(["type"],filterByTag);
}

async function loadPages() {
    const sort = document.getElementById('list-sort').value;
    let dir = 'asc';
    if (sort==='updatedAt'){
        dir = 'desc';
    }
    const res = await fetch(`/api/pages?sort=${sort}&direction=${dir}`);
    const allPages = await res.json();
    renderPages(allPages, pageMap);

}


// 태그 필터링
async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const sort = document.getElementById('list-sort').value;

    if (selected.length === 0) {
        loadPages();
    } else {
        const type = selected.map(e => e.dataset.id);
        const params = new URLSearchParams();

        type.forEach(id => params.append('trialDesign', id));

        const res = await fetch(`/api/pages/filter?${params.toString()}&sort=${sort}&direction=asc`);
        const filtered = await res.json();

        renderPages(filtered, pageMap);
    }
}

// 페이지 추가
document.getElementById('insert-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;

    const data = {
        title: form.title.value,
        link: form.link.value,
        level: form.level.value
    };

    console.log("보내는 데이터", data);

    const res = await fetch(`/api/pages`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    const text = await res.text();
    console.error("응답 본문", text);

    if (res.ok) {
        alert("페이지가 추가되었습니다.");
        form.reset();
        loadPages();
    } else {
        alert("추가 실패");
    }
});


