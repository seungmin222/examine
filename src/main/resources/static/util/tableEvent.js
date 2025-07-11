import {
    renderEffectCache,
    checkCheckboxes,
    ArrayCheckboxesById,
} from '/util/utils.js';

function journalEvent(journalMap, loadJournals, loadTags=()=>{}){
    document.getElementById('journal-body').addEventListener('click', async e => {
        const row = e.target.closest('tr');
        const itemId = Number(row?.dataset.id);
        const item = journalMap.get(itemId);
        const modal = document.getElementById('modal');
        const modalId = parseInt(modal?.dataset.id);
        if (!row||!itemId) return;

        // 삭제 모드
        if (document.getElementById('toggle-delete')?.classList.contains('execute')) {
            e.preventDefault();
            const title = row.querySelector('a')?.textContent.trim();
            if (confirm(`'${title}' 논문을 삭제할까요?`)) {
                await fetch(`/api/journals/${itemId}`, {
                    method: 'DELETE'
                });
                await loadJournals();
            }
            return;
        }
        // 태그 모달
        if (e.target.classList.contains('modal-btn')) {
            if (itemId !== modalId) {
                const button = document.querySelector(`.modal-btn[data-id="${modalId}"]`);
                const newButton = document.querySelector(`.modal-btn[data-id="${itemId}"]`);
                if (button) {
                    button.classList.remove('execute');
                }
                if (newButton) {
                    newButton.classList.add('execute');
                }
                modal.dataset.id = itemId;
                renderEffectCache(item);
            }
            modal.classList.remove('hidden');
        }

        // 저장 버튼
        if (e.target.classList.contains('save-btn')) {
            let effects, sideEffects;

            if (itemId !== modalId) {
                effects = item.effects.map(i => ({
                    supplementId: i.supplementId,
                    effectId: i.effectId,
                    cohenD: i.cohenD,
                    pearsonR: i.pearsonR,
                    pValue: i.pValue
                }));

                sideEffects = item.sideEffects.map(i => ({
                    supplementId: i.supplementId,
                    effectId: i.effectId,
                    cohenD: i.cohenD,
                    pearsonR: i.pearsonR,
                    pValue: i.pValue
                }));
            } else {
                effects = [...document.querySelectorAll('.effect-cash')].map(c => ({
                    supplementId: parseInt(c.dataset.supplementId),
                    effectId: parseInt(c.dataset.effectId),
                    cohenD: parseFloat(c.querySelector('input[name="cohenD"]')?.value),
                    pearsonR: parseFloat(c.querySelector('input[name="peasonR"]')?.value),
                    pValue: parseFloat(c.querySelector('input[name="pValue"]')?.value)
                }));

                sideEffects = [...document.querySelectorAll('.sideEffect-cash')].map(c => ({
                    supplementId: parseInt(c.dataset.supplementId),
                    effectId: parseInt(c.dataset.effectId),
                    cohenD: parseFloat(c.querySelector('input[name="cohenD"]')?.value),
                    pearsonR: parseFloat(c.querySelector('input[name="pearsonR"]')?.value),
                    pValue: parseFloat(c.querySelector('input[name="pValue"]')?.value)
                }));
            }

            const updated = {
                link: item.link,
                trialDesignId: parseInt(row.querySelector('[name="trialDesign"]').value),
                blind: parseInt(row.querySelector('[name="blind"]').value),
                parallel: row.querySelector('[name="parallel"]').value,
                durationValue: parseInt(row.querySelector('[name="durationValue"]').value),
                durationUnit: row.querySelector('[name="durationUnit"]').value,
                participants: parseInt(row.querySelector('[name="participants"]').value),
                effects,
                sideEffects
            };

            const res = await fetch(`/api/journals/${itemId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(updated)
            });

            if (res.ok) {
                alert("논문이 수정되었습니다.");
                //모달 초기화 및 테이블 재랜더링
                loadTags();
                loadJournals();
                modal.dataset.id = '';
            } else {
                alert("수정 실패");
            }
        }
    });
}

function supplementEvent(supplementMap, loadSupplements, loadTags=()=>{}) {
    document.getElementById('supplement-body').addEventListener('click', async e => {
        const row = e.target.closest('tr');
        const itemId = Number(row?.dataset.id);
        const item = supplementMap.get(itemId);
        const modal = document.getElementById('modal');
        const modalId = parseInt(modal?.dataset.id);
        if (!row||!itemId) {
            return;
        }
        // 삭제 모드
        if (document.getElementById('toggle-delete')?.classList.contains('execute')) {
            e.preventDefault(); // 링크 이동 방지
            if (confirm(`'${item.korName}' 을 삭제할까요?`)) {
                await fetch(`/api/supplements/${itemId}`, {
                    method: 'DELETE'
                });
                await loadSupplements();
            }
            return;
        }
        // 태그 모달
        if (e.target.classList.contains('modal-btn')) {
            if (itemId !== modalId) {
                const button = document.querySelector(`.modal-btn[data-id="${modalId}"]`);
                const newButton = document.querySelector(`.modal-btn[data-id="${itemId}"]`);
                if (button) {
                    button.classList.remove('execute');
                }
                if (newButton) {
                    newButton.classList.add('execute');
                }
                modal.dataset.id = itemId;

                checkCheckboxes('type', item.types);
            }
            modal.classList.remove('hidden');
        }

        // 저장 버튼
        if (e.target.classList.contains('save-btn')) {
            const typeIds = ArrayCheckboxesById('type');
            const updated = {
                korName: row.querySelector('[name="korName"]').value,
                engName: row.querySelector('[name="engName"]').value,
                dosageValue: row.querySelector('[name="dosageValue"]').value,
                dosageUnit: row.querySelector('[name="dosageUnit"]').value,
                cost: parseFloat(row.querySelector('[name="cost"]').value),
                typeIds
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
                loadTags();
                modal.dataset.id = '';
            } else {
                alert('저장 실패');
            }
        }

    });
}

function pageEvent(pageMap, loadPages){
    document.getElementById('page-body').addEventListener('click', async e => {
        const row = e.target.closest('tr');
        const itemId = Number(row?.dataset.id);
        const item = pageMap.get(itemId);
        if (!row||!itemId) return;

        // 삭제 모드
        if (document.getElementById('toggle-delete')?.classList.contains('execute')) {
            e.preventDefault();
            const title = row.querySelector('a')?.textContent.trim();
            if (confirm(`'${title}' 페이지를 목록에서 삭제할까요?`)) {
                await fetch(`/api/pages/${itemId}`, {
                    method: 'DELETE'
                });
                await loadPages();
            }
            return;
        }
        // 저장 버튼
        if (e.target.classList.contains('save-btn')) {

            const updated = {
                title: row.querySelector('[name="title"]').value,
                link: row.querySelector('[name="link"]').value,
                level: row.querySelector('[name="level"]').value
            };

            const res = await fetch(`/api/pages/${itemId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(updated)
            });

            if (res.ok) {
                alert("페이지가 수정되었습니다.");
                //모달 초기화 및 테이블 재랜더링
                loadPages();
            } else {
                alert("수정 실패");
            }
        }
    });
}

function tagTableEvent(loadEffects,type){
    document.getElementById('tag-body').addEventListener('click', async e => {
        const row = e.target.closest('tr');
        const itemId = Number(row?.dataset.id)
        if (!row||!itemId) return;

        // 삭제 모드
        if (document.getElementById('toggle-delete')?.classList.contains('execute')) {
            e.preventDefault();
            const name = row.querySelector('[name="korName"]')?.textContent.trim();
            if (confirm(`'${name}' 태그를 삭제할까요?`)) {
                await fetch(`/api/tags/${type}/${itemId}`, {
                    method: 'DELETE'
                });
                await loadEffects();
            }
            return;
        }
    });
}

export {
    supplementEvent,
    journalEvent,
    pageEvent,
    tagTableEvent
};