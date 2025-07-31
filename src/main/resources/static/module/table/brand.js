import {
    setupFoldToggle,
    setupSortTrigger,
    setupSearchForm,
    renderBrands,
    loadButton,
    brandEvent,
    createNewAlarm
} from '/util/index.js';


const brandMap = new Map();

// 초기 로딩
export async function init() {

    await loadButton(loadBrands);
    await loadBrands();
    // 테이블 수정, 삭제
    brandEvent(brandMap, loadBrands);

    // 접기 토글
    setupFoldToggle('toggle-fold', loadBrands);

    // 정렬
    setupSortTrigger('list-sort', loadBrands);

    setupSearchForm('brand', "search-form", "list-sort", ['brand'], renderBrands); //api 주소 수정 필요
}

async function loadBrands() {
    const sort = document.getElementById('list-sort')?.value;
    let dir = 'desc';

        const res = await fetch(`/api/brand?sort=${sort}&direction=${dir}`);
        const data = await res.json();

        if (!Array.isArray(data)) {
            throw new Error('응답 형식이 배열이 아님');
        }

        renderBrands(data, brandMap);
}

document.getElementById('brand-insert-form').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;

    const data = {
        korName: form.korName.value,
        engName: form.engName.value,
        country: form.country.value,
        fei: form.fei.value || null,
        nai: form.nai.value ? parseInt(form.nai.value) : null,
        vai: form.vai.value ? parseInt(form.vai.value) : null,
        oai: form.oai.value ? parseInt(form.oai.value) : null
    };

    try {
        const res = await fetch('/api/brand', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (res.ok) {
            createNewAlarm("브랜드가 추가되었습니다.");
            await loadBrands();
        } else {
            alert("브랜드 추가 실패");
        }
    } catch (err) {
        console.error("브랜드 추가 오류:", err);
        alert("에러 발생");
    }
});
