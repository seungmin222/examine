import {
    createModalInner,
    createTagList,
    checkCheckboxesById,
    ArrayCheckboxesById
} from '/util/utils.js';

import {
    setupFoldToggle,
    setupSortTrigger,
    setupToggleButton,
    setupModalOpenClose,
    setupSearchForm,
    resetButton
} from '/util/eventUtils.js';

import {
    loadBasic
} from '/util/load.js';

// 초기 로딩
document.addEventListener('DOMContentLoaded', async () => {

    await loadBasic();
    await loadSamples();
    await loadTags();

    // 접기 토글
    setupFoldToggle('toggle-fold', loadSamples);
    setupFoldToggle('tag-toggle-fold', loadTags);

    // 정렬
    setupSortTrigger('list-sort', loadSamples);
    setupSortTrigger('tag-sort', loadTags);
    setupSortTrigger('modal-sort', loadTags);

    // 삭제 / 수정 토글
    setupToggleButton('toggle-delete', 'toggle-change', loadSamples);

    // 모달 열고 닫기
    setupModalOpenClose('modal-open', 'modal-close', 'modal');
    resetButton("tags", "modal-reset", "modal-sort", ["", ""], renderModal);

    setupSearchForm("tags", "tag-search-form", "tag-sort", ["", ""], renderTags);
    setupSearchForm("tags", "modal-search-form", "modal-sort", ["", ""], renderModal);
    setupSearchForm("samples", "search-form", "list-sort", null, renderSamples);
});

async function loadJournals() {
    const sort = document.getElementById('list-sort').value;
    const res = await fetch(`/api/samples?sort=${sort}&direction=asc`);
    const allSamples = await res.json();
    renderSamples(allSamples);
}

function renderSamples(list) {
    const tbody = document.getElementById('sample-body');
    tbody.innerHTML = '';
    const folded = document.getElementById('fold-toggle') ? .classList.contains('folded');
    const shown = folded ? list.slice(0, 5) : list;
    const editMode = document.getElementById('toggle-change') ? .classList.contains('execute');
    const deleteMode = document.getElementById('toggle-delete') ? .classList.contains('execute');

    shown.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
      <td> // 수정모드일때는 input, select로 랜더링
        ${editMode
          ? `<input value="${item.}" name=""/>`
          : item.}
      </td>
      ${editMode ? `<td>
         <div class="edit-group"> // 모달, 저장 버튼 랜더링
            <button class="modal-btn" data-id="${item.id}">태그</button>
            <button class="save-btn" data-id="${item.id}">저장</button>
         </div>
      </td>`: ''}
      `;

        row.addEventListener('click', async e => {
            if (deleteMode) {
                e.preventDefault(); // 링크 이동 방지
                if (confirm(`'${item.}' 을 삭제할까요?`)) {
                    await fetch(`/api/samples/${item.id}`, {
                        method: 'DELETE'
                    });
                    await loadSamples();
                }
                return;
            } else if (e.target.classList.contains('save-btn')) {
                const updated = {

                };

                const res = await fetch(`/api/samples/${item.id}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(updated)
                });

                if (res.ok) {
                    alert("논문이 수정되었습니다.");
                    loadJournals();
                } else {
                    alert("수정 실패");
                }
            } else if (e.target.classList.contains('modal-btn')) {
                checkCheckboxesById('', item.); // 체크박스에 기존 정보 불러오기

                document.getElementById('modal').style.display = 'block'; // 모달 여닫기
                const closeButton = document.getElementById('modal-close');
                const saveButton = document.getElementById('modal-save');
                saveButton.style.display = 'inline-block';
                closeButton.addEventListener('click', () => {
                    document.getElementById('modal-save').style.display = 'none';
                });
                const newSaveButton = saveButton.cloneNode(true);
                saveButton.replaceWith(newSaveButton);

                newSaveButton.addEventListener('click', async e => { // 저장

                    const updated = {};

                    const res = await fetch(`/api/samples/${item.id}`, {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(updated)
                    });

                    if (res.ok) {
                        alert("논문이 수정되었습니다.");
                        await loadSamples();
                    } else {
                        alert("수정 실패");
                    }
                });
            }
        });
        tbody.appendChild(row);
    });
}

// 태그 로딩
async function loadTags() {
    const sort = document.getElementById('tag-sort').value;
    const allTypes = ['', ''];
    const allTierTypes = ['', ''];
    for (let type of allTypes) {
        const res = await fetch(`/api/tags?type=${type}&sort=${encodeURIComponent(sort)}&direction=asc`);
        const list = await res.json();
        renderTags(type, list);
        renderModal(type, list);
    }
    for (let type of allTierTypes) {
        const res = await fetch(`/api/tags/tier?type=${type}`);
        const list = await res.json();
        renderTags(type, list);
        renderModal(type, list);
    }
}

function renderTags(type, list) {
    let tagList = document.getElementById(`${type}-list`);
    if (!tagList) {
        console.warn(`❌ 알 수 없는 type: ${type}`);
        return;
    }
    tagList.innerHTML = '';

    const folded = document.getElementById('tag-toggle-fold') ? .classList.contains('folded');
    const shown = folded ? list.slice(0, 5) : list;

    const items = createTagList(type, shown);
    items.forEach(li => tagList.appendChild(li));

    // 처음 한 번만 등록
    if (!tagList.dataset.listenerAttached) {
        tagList.addEventListener('click', async e => {
            if (e.target.tagName === 'LI') {
                e.target.classList.toggle('selected');
                filterByTag();
            }
        });
        tagList.dataset.listenerAttached = "true";
    }

}


function renderModal(type, list) {
    const sort = document.getElementById('modal-sort').value;

    const container = document.getElementById(`${type}-checkboxes`);
    if (container) {
        container.innerHTML = '';
        list.forEach(tag => {
            const item = createModalInner(tag, type);
            container.appendChild(item);
        });
    } else {
        console.warn(`❌ 알 수 없는 type: ${type}`);
        return;
    }
}

// 태그 필터링
async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const sort = document.getElementById('list-sort').value;

    if (selected.length === 0) {
        loadSamples();
    } else {

        const = selected.filter(e => e.dataset.type === '').map(e => e.dataset.id);
        const params = new URLSearchParams();
        .forEach(id => params.append('', id));
        const res = await fetch(`/api/journals/filter?${params.toString()}&sort=${sort}&direction=asc`);
        const filtered = await res.json();

        renderSamples(filtered);

    }
}

// 논문 추가
document.getElementById('insert-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;

    const = ArrayCheckboxesById('');

    const data = {};

    console.log("보내는 데이터", data);

    const res = await fetch(`/api/samples`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    const text = await res.text();
    console.error("응답 본문", text);

    if (res.ok) {
        alert("논문이 추가되었습니다.");
        form.reset();
        loadSamples();
    } else {
        alert("추가 실패");
    }
});