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
    setupPairToggleButton,
    setupModalOpenClose,
    setupSearchForm,
    resetButton
} from '/util/eventUtils.js';

import {
    renderSupplements,
    renderTags,
    renderModal
} from '/util/render.js';

import {
    loadBasic
} from '/util/load.js';


// 초기 로딩
document.addEventListener('DOMContentLoaded', async e => {

    await loadBasic();
    await loadSupplements();
    await loadTags();

    // 접기 토글
    setupFoldToggle('toggle-fold', loadSupplements);
    setupFoldToggle('tag-toggle-fold', loadTags);

    // 정렬
    setupSortTrigger('list-sort', loadSupplements);
    setupSortTrigger('tag-sort', loadTags);
    setupSortTrigger('modal-sort', loadTags);

    // 삭제 / 수정 토글
    setupPairToggleButton('toggle-delete', 'toggle-change', loadSupplements);
    setupPairToggleButton('tag-toggle-delete', '', loadTags); // 수정버튼 없는 경우도 처리 가능 // 단독 토글일 경우 self로 전달

    // 모달 열고 닫기
    setupModalOpenClose('modal-open', 'modal-close', 'modal');
    resetButton("tags", "modal-reset", "modal-sort", ["type", "positive", "negative"], renderModal);

    //검색 폼
    setupSearchForm("tags", "tag-search-form", "tag-sort", ["type", "positive", "negative"], renderTags);
    setupSearchForm("tags", "modal-search-form", "modal-sort", ["type", "positive", "negative"], renderModal);
    setupSearchForm("supplements", "search-form", "list-sort", null, renderSupplements);
});


async function loadSupplements() {
    const sort = document.getElementById('list-sort').value;
    const res = await fetch(`/api/supplements?sort=${sort}&direction=asc`);
    const allSupplements = await res.json();
    renderSupplements(allSupplements);
}



document.getElementById('supplement-body').addEventListener('click', async e => {
    const row = e.target.closest('tr');
    if (!row) return;

    const itemId = e.target.dataset.id;

    // 삭제 모드
    if (document.getElementById('toggle-delete') ?.classList.contains('execute')) {
        e.preventDefault(); // 링크 이동 방지
        if (confirm(`'${item.korName}' 을 삭제할까요?`)) {
           await fetch(`/api/supplements/${itemId}`, {
           method: 'DELETE'
           });
           await loadSupplements();
        }
         return;
    }

    // 저장 버튼
    if (e.target.classList.contains('save-btn')) {
        const types = ArrayCheckboxesById('type');
        const updated = {
              id: itemId,
              korName: row.querySelector('[name="korName"]').value,
              engName: row.querySelector('[name="engName"]').value,
              dosage: row.querySelector('[name="dosage"]').value,
              cost: parseFloat(row.querySelector('[name="cost"]').value),
              types
        };

        const res = await fetch(`/api/supplements/${item.id}`, {
              method: 'PUT',
              headers: {
              'Content-Type': 'application/json'
              },
              body: JSON.stringify(updated)
        });

        if (res.ok) {
              alert('저장되었습니다');
              await loadSupplements();
              document.getElementById('list-sort')?.dispatchEvent(new Event('change'));
        } else {
              alert('저장 실패');
        }
    }
    // 태그 모달
    if (e.target.classList.contains('modal-btn')) {
        //checkCheckboxesById('type', e.taget.types);
        document.getElementById('modal').style.display = 'block';
    }
});

// 태그 로딩
async function loadTags() {
    const sort = document.getElementById('tag-sort').value;
    const allTypes = ['type', 'positive', 'negative'];

    for (let type of allTypes) {
        const res = await fetch(`/api/tags?type=${type}&sort=${encodeURIComponent(sort)}&direction=asc`);
        const list = await res.json();
        renderTags(type, list);
        renderModal(type, list);
    }
}

// 태그 필터링
async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const sort = document.getElementById('list-sort').value;

    if (selected.length === 0) {
        loadSupplements();
    } else {
        const typeIds = selected.filter(e => e.dataset.type === 'type').map(e => e.dataset.id);
        const effectIds = selected.filter(e => e.dataset.type === 'positive').map(e => e.dataset.id);
        const sideEffectIds = selected.filter(e => e.dataset.type === 'negative').map(e => e.dataset.id);

        const params = new URLSearchParams();
        typeIds.forEach(id => params.append('typeIds', id));
        effectIds.forEach(id => params.append('effectIds', id));
        sideEffectIds.forEach(id => params.append('sideEffectIds', id));

        const res = await fetch(`/api/supplements/filter?${params.toString()}&sort=${sort}&direction=asc`);
        const filtered = await res.json();

        renderSupplements(filtered);
    }
}

// 성분 추가
document.getElementById('insert-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;

    const types = ArrayCheckboxesById('type');
    const effects = ArrayCheckboxesById('positive');
    const sideEffects = ArrayCheckboxesById('negative');

    const data = {
        korName: form.korName.value,
        engName: form.engName.value,
        types,
        dosage: form.dosage.value,
        cost: parseFloat(form.cost.value),
        effects,
        sideEffects
    };

    const res = await fetch('/api/supplements', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (res.ok) {
        alert("성분이 추가되었습니다.");
        form.reset();
        // 효과 체크박스 및 셀렉트 초기화
        document.querySelectorAll('#positive-checkboxes div').forEach(wrapper => {
            const checkbox = wrapper.querySelector('input[type="checkbox"]');
            const select = wrapper.querySelector('select');

            checkbox.checked = false;
            select.value = 'null'; // 기본 옵션으로 되돌림
        });
        document.querySelectorAll('#negative-checkboxes div').forEach(wrapper => {
            const checkbox = wrapper.querySelector('input[type="checkbox"]');
            const select = wrapper.querySelector('select');

            checkbox.checked = false;
            select.value = 'null'; // 기본 옵션으로 되돌림
        });
        await loadSupplements();
    } else {
        alert("추가 실패");
    }
});

// 태그 추가
document.getElementById('tag-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const data = {
        name: form.tagName.value,
        type: form.tagType.value
    };

    const res = await fetch('/api/tags', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (res.ok) {
        alert("태그가 추가되었습니다.");
        form.reset();
        await loadTags();
    } else {
        alert("태그 추가 실패");
    }
});