import {
    loadButton,
    selectList,
    setupModalOpenClose,
    resetButton,
    createNewAlarm,
    journalEvent,
    supplementEvent,
    productEvent,
    renderSupplements,
    renderJournals,
    renderDetails,
    renderTagTable,
    renderProducts,
    renderTags,
    renderModal,
    renderButton,
} from '/util/index.js';

const journalMap = new Map();
const supplementMap = new Map();
const productMap = new Map();

const params = new URLSearchParams(window.location.search);
const supplementId = params.get('id');

export async function init() {
    if (!supplementId) {
        return;
    }
    try {
        await loadButton(loadAll);
        await loadAll();
        renderButton('button-box','save-button','저장','');
        //supplementEvent(supplementMap,loadDetails);
        //journalEvent(journalMap,loadJournals);
        productEvent(productMap,loadProducts);
        setupModalOpenClose('modal-open', 'modal-close', 'modal');
        resetButton("tags", "modal-reset", "modal-sort", ["brand"], renderModal);
        selectList(["brand"],filterByTag);

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
    const data = {
        id : parseInt(supplementId)
    };
    document.querySelectorAll('.editable').forEach(el => {
        const key = el.dataset.field;
        data[key] = el.innerText.trim(); // 줄바꿈이 있다면 innerHTML로 바꿔도
    });

    const res = await fetch(`/api/supplements/detail`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });

    if (res.ok) {
        createNewAlarm('✅ 수정 완료!');
        loadDetails(); // 서버로부터 다시 불러오기
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

async function loadProducts(){
    const sort = document.getElementById('product-list-sort').value;
    let dir = 'desc';
    if (sort === 'name'){
        dir = 'asc';
    }
    const res = await fetch(`/api/supplements/detail/${supplementId}/products?sort=${sort}&direction=${dir}`);
    const products = await res.json();
    renderProducts(products, productMap);
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
    renderSupplements(details.supplement, supplementMap);
    renderDetails(details.detail);
    renderTagTable(details.effects, 'effect' ,'effect-body');
    renderTagTable(details.sideEffects, 'sideEffect', 'sideEffect-body');
}

async function loadAll(){
    await loadDetails();
    await loadJournals();
    await loadProducts();
    await loadTags();
}

document.getElementById("insert-form").addEventListener("submit", async (e) => {
    e.preventDefault();

    const form = e.target;

    const data = {
        supplementId: supplementId,
        link: form.link.value,
        name: form.name.value,
        dosageValue: parseFloat(form.dosageValue.value),
        dosageUnit: form.dosageUnit.value,
        price: parseFloat(form.price.value),
        pricePerDose: parseFloat(form.pricePerDose.value),
    };

    try {
        const res = await fetch("/api/supplements/detail/products", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        });

        if (res.ok) {
            createNewAlarm("✅ 제품 등록 완료!");
            await loadProducts();
            form.reset();
        } else {
            const msg = await res.text();
            alert("❌ 실패: " + msg);
        }
    } catch (err) {
        console.error("에러 발생:", err);
        alert("🚨 요청 실패");
    }
});

async function loadTags() {
    const sort = document.getElementById('tag-sort').value;
    const allTypes = ['brand'];

    const query = new URLSearchParams({
        type: allTypes.join(','), //
        sort: sort,
        direction: 'asc'
    }).toString();

    const res = await fetch(`/api/tags?${query}`);
    const tagMap = await res.json();

    for (const [type, list] of Object.entries(tagMap)) {
        renderTags(type, list);
        renderModal(type, list);
    }
}

async function filterByTag() {
    const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
    const sort = document.getElementById('product-list-sort').value;

    if (selected.length === 0) {
        loadProducts();
    } else {
        const brandIds = selected.filter(e => e.dataset.type === 'brand').map(e => e.dataset.id);
        const params = new URLSearchParams();
        brandIds.forEach(id => params.append('brandIds', id));

        const res = await fetch(`/api/supplements/filter?${params.toString()}&sort=${sort}&direction=asc`);
        const filtered = await res.json();

        renderProducts(filtered,productMap);
    }
}
