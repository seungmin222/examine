function createTagList(type, list) {
  const liList = list.map(tag => {
    const li = document.createElement('li');
    li.textContent = tag.name;
    li.dataset.id = tag.id;
    li.dataset.type = type;
    return li;
  });

  if (type === 'trialDesign') {
    liList.forEach((li, i) => {
      const tier = list[i]?.tier;
      if (tier) {
        li.classList.add(tier[0]);
      }
    });
  }

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
  span.textContent = tag.name;
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

function createTooltip(anchorId, text, position = 'top') {
  const anchor = document.getElementById(`${anchorId}`);
  if (!anchor) {
    console.warn(`Tooltip anchor not found: ${anchorId}`);
    return;
  }
  const box = document.createElement('div');
  box.classList.add('tooltip-box');
  box.classList.add('hidden');
  box.dataset.position = position;

  const body = document.createElement('div');
  body.className = 'tooltip-body';
  body.innerHTML = text;

  const tail = document.createElement('div');
  tail.className = 'tooltip-tail';

  const tailBorder = document.createElement('div');
  tailBorder.className = 'tooltip-tail-border';

  box.appendChild(body);
  box.appendChild(tail);
  box.appendChild(tailBorder);
  anchor.appendChild(box);

  anchor.addEventListener("mouseenter", () => {
      box.classList.remove('hidden');
  });
  anchor.addEventListener("mouseleave",() => {
      box.classList.add('hidden');
  });
}




function checkCheckboxesById(type, tags = [], idField = 'id') {
  if(tags == null){
    return;
  }
  const tagArray = Array.isArray(tags) ? tags : [tags];  // 단일 객체도 배열로 포장
  const idSet = new Set(tagArray.map(e => e[idField]));

  document.querySelectorAll(`#${type}-checkboxes input`).forEach(cb => {
    cb.checked = idSet.has(+cb.value);
  });
}


function checkCheckboxes(type, tag) {
  document.querySelectorAll(`#${type}-checkboxes input`).forEach(cb => {
    cb.checked = (tag === cb.value);
  });
}


function ArrayCheckboxesById(type) {
    return Array.from(document.querySelectorAll(`#${type}-checkboxes input:checked`))
              .map(cb => ({ id: parseInt(cb.value) }));
}

function ObjectCheckboxesById(type) {
    const checked = document.querySelector(`#${type}-checkboxes input:checked`);
    return checked ? checked.value : null;
}

function ArrayCheckboxesByName(type) {
  return Array.from(document.querySelectorAll(`#${type}-checkboxes input:checked`))
    .map(cb => ({
      id: parseInt(cb.value),
      name: cb.nextElementSibling?.textContent.trim() ?? '(이름 없음)'
    }));
}

function resetModal(types){
   types.forEach(type => {
      const boxes = document.querySelectorAll(`#${type}-checkboxes input[type="checkbox"]`);
      boxes.forEach(box => box.checked = false);
    });
}


export {
  createTierSelectBox,
  createModalInner,
  createTagList,
  checkCheckboxes,
  checkCheckboxesById,
  ArrayCheckboxesById,
  ObjectCheckboxesById,
  ArrayCheckboxesByName,
  resetModal,
  createTooltip
};