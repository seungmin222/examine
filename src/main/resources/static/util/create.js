import {
    createPopper
} from 'https://unpkg.com/@popperjs/core@2/dist/esm/popper.js';

function createTagList(type, list) {
    const liList = list.map(tag => {
        const li = document.createElement('li');
        li.textContent = tag.korName;
        li.dataset.name = tag.engName;
        li.dataset.id = tag.id;
        li.dataset.type = type;
        li.classList.add(tag.tier[0]);
        return li;
    });
    return liList;
}

// tag 배열 받아서 한꺼번에 만들기
function createCheckbox(tag, prefix) {
    const cb = document.createElement('input');
    cb.type = 'checkbox';
    cb.value = tag.id;
    cb.id = `${prefix}-${tag.id}`;
    return cb;
}

function createLabelText(tag) {
    const span = document.createElement('span');
    span.textContent = tag.korName;
    const tier = tag?.tier;
    if (tier) {
        span.classList.add(tier[0]);
    }
    return span;
}

function createTierSelectBox(tagId, prefix) {
    const gr = document.createElement('select');
    gr.name = `tier-${tagId}`;
    gr.id = `${prefix}-tier-${tagId}`;
    gr.className = 'modal';

    ['A', 'B', 'C', 'D', 'null'].forEach(g => {
        const opt = document.createElement('option');
        opt.value = g;
        opt.className = g;
        opt.textContent = g;
        if (g === 'null') opt.selected = true;
        gr.appendChild(opt);
    });

    gr.addEventListener('change', () => {
        gr.className = gr.value;
    });

    return gr;
}

function createModalInner(tag, type) {
    const wrapper = document.createElement('label');
    wrapper.classList.add('modal-inner');
    const cb = createCheckbox(tag, type);
    const labelText = createLabelText(tag);

    wrapper.append(cb, labelText);

    return wrapper;
}

//popper 라이브러리 사용
function createTailTooltip(anchorId, text, position = 'top', cls = '') {
    const anchor = document.getElementById(anchorId);
    if (!anchor) {
        console.warn(`Tooltip anchor not found: ${anchorId}`);
        return;
    }

    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip-box hidden';
    if (cls) {
        tooltip.classList.add(cls);
    }
    tooltip.innerHTML = `
    <div class="tooltip-body">${text}</div>
    <div class="tooltip-tail-wrapper" data-popper-arrow>
      <div class="tooltip-tail-border"></div>
      <div class="tooltip-tail"></div>
    </div>
  `;
    document.body.appendChild(tooltip);

    const popperInstance = createPopper(anchor, tooltip, {
        placement: position,
        modifiers: [
            {
                name: 'offset',
                options: { offset: [0, 10] },
            },
            {
                name: 'arrow',
                options: {
                    element: tooltip.querySelector('[data-popper-arrow]'),
                },
            },
            {
                name: 'preventOverflow',
                options: {
                    boundary: 'viewport',
                },
            },
        ],
    });

    let hideTimer = null;
    const show = () => {
        clearTimeout(hideTimer);
        tooltip.classList.remove('hidden');
        popperInstance.update();
    };
    const hide = () => {
        hideTimer = setTimeout(() => {
            tooltip.classList.add('hidden');
        }, 100);
    };

    anchor.addEventListener('mouseenter', show);
    anchor.addEventListener('mouseleave', hide);
    tooltip.addEventListener('mouseenter', show);
    tooltip.addEventListener('mouseleave', hide);
}

function createSimpleTooltip(anchorId, text, position = 'top', cls = '') {
    const anchor = document.getElementById(anchorId);
    if (!anchor) {
        console.warn(`Tooltip anchor not found: ${anchorId}`);
        return;
    }

    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip-box hidden';
    if (cls) tooltip.classList.add(cls);

    tooltip.innerHTML = `<div class="tooltip-body">${text}</div>`;
    document.body.appendChild(tooltip);

    const popperInstance = createPopper(anchor, tooltip, {
        placement: position,
        modifiers: [
            {
                name: 'offset',
                options: { offset: [0, 10] },
            },
            {
                name: 'preventOverflow',
                options: {
                    boundary: 'viewport',
                },
            },
        ],
    });

    let hideTimer = null;
    const show = () => {
        clearTimeout(hideTimer);
        tooltip.classList.remove('hidden');
        popperInstance.update();
    };
    const hide = () => {
        hideTimer = setTimeout(() => {
            tooltip.classList.add('hidden');
        }, 100);
    };

    anchor.addEventListener('mouseenter', show);
    anchor.addEventListener('mouseleave', hide);
    tooltip.addEventListener('mouseenter', show);
    tooltip.addEventListener('mouseleave', hide);
}

function createNumberSVG(num) {
    const svgNS = "http://www.w3.org/2000/svg";
    const svg = document.createElementNS(svgNS, "svg");

    svg.setAttribute("width", "20");
    svg.setAttribute("height", "20");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("class", "number-icon");

    // 배경 원 (또는 사각형으로 바꿔도 됨)
    const rect = document.createElementNS(svgNS, "rect");
    rect.setAttribute("x", "2");           // 시작 x좌표
    rect.setAttribute("y", "2");           // 시작 y좌표
    rect.setAttribute("width", "20");      // 너비
    rect.setAttribute("height", "20");     // 높이
    rect.setAttribute("rx", "5");          // 둥근 모서리 반지름 (선택)
    rect.setAttribute("fill", "var(--color-button)");

    svg.appendChild(rect);

    // 숫자 텍스트
    const text = document.createElementNS(svgNS, "text");
    text.setAttribute("x", "12");
    text.setAttribute("y", "16");
    text.setAttribute("text-anchor", "middle");
    text.setAttribute("font-size", "12");
    text.setAttribute("fill", "white");
    text.setAttribute("font-weight", "bold");
    text.textContent = num;
    svg.appendChild(text);

    return svg;
}

function createPath(){
    const path = window.location.pathname.replace(/^\/+|\/+$/g, ''); // trim slashes
    const segments = path.split('/'); // ex: ['table', 'journal']

    let base = segments.join('/');
    if (base === '') base = 'home';
    return base;
}

function createCopyIcon(text) {
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("width", "20");
    svg.setAttribute("height", "20");
    svg.classList.add("copy-icon");

    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
    path.setAttribute("d", "M16 1H4C2.9 1 2 1.9 2 3v14h2V3h12V1zM18 5H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h10v14z");
    path.setAttribute("style", "fill:#16a34a;stroke:none");

    svg.appendChild(path);

    svg.addEventListener("click", () => {
        navigator.clipboard.writeText(text)
            .then(() => alert(`복사됨: ${text}`))
            .catch(() => alert("복사 실패"));
    });

    return svg;
}

function createIherbCoupon(coupon) {
    const li = document.createElement("li");
    li.className = "w-full h-fit cursor-default";

    const detail = document.createElement("p");
    detail.textContent = `📢 ${coupon.detail}`;

    const code = document.createElement("p");
    if (coupon.promoCode) {
        code.textContent = `💸 코드: ${coupon.promoCode} `;
        code.appendChild(createCopyIcon(coupon.promoCode));
    } else {
        code.textContent = `❕ 할인코드 없음`;
    }

    const link = document.createElement("a");
    link.href = coupon.link;
    link.textContent = "👉 쇼핑하러 가기";
    link.target = "_blank";

    const expires = document.createElement("p");
    if (coupon.expiresAt) {
        const date = new Date(coupon.expiresAt);
        expires.textContent = `⏳ 종료: ${date.toLocaleString("ko-KR")}`;
    }

    expires.appendChild(link);

    li.append(detail, code, expires);
    return li;
}

function createA(href, text, cls, rel = "noopener noreferrer") {
    const link = document.createElement("a");
    link.href = href;
    link.textContent = text;
    link.rel = rel;
    if (cls) link.classList.add(cls);
    return link;
}

function createImage(src, alt = "", cls = "", fallback = "/image/placeholder.png") {
    const img = document.createElement("img");
    img.src = src || fallback;
    img.alt = alt;

    if (cls) {
        img.classList.add(...cls.split(" "));
    }

    return img;
}


function createProduct(userProduct) {
    const product = userProduct.product;
    const wrapper = document.createElement("div");
    wrapper.classList.add("product-item", "flex", "gap-4", "items-center", "p-2", "border", "rounded");
    wrapper.dataset.id = product.id;

    const img = createImage(product.imageUrl, product.name, "w-20 h-20 object-cover rounded");

    const info = document.createElement("div");
    info.classList.add("flex", "flex-col");

    const nameLink = createA(product.link, product.name, "text-blue-600 underline");

    const dosage = document.createElement("span");
    dosage.textContent = `${product.dosageValue ?? "?"} ${product.dosageUnit ?? ""}`;

    const price = document.createElement("span");
    price.textContent = `₩${product.price?.toLocaleString() ?? "?"}`;

    const unitPrice = document.createElement("span");
    unitPrice.textContent = `복용량당 ₩${product.pricePerDose?.toLocaleString() ?? "?"}`;

    const quantity = document.createElement("input");
    quantity.type = "number";
    quantity.value = userProduct.quantity;
    quantity.classList.add("w-16", "border", "text-center");

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.checked = userProduct.isChecked;

    info.append(nameLink, dosage, price, unitPrice, quantity);
    wrapper.append(checkbox, img, info);

    return wrapper;
}




export {
    createTierSelectBox,
    createModalInner,
    createTagList,
    createTailTooltip,
    createSimpleTooltip,
    createNumberSVG,
    createPath,
    createCopyIcon,
    createIherbCoupon,
    createA,
    createImage,
    createProduct,
};