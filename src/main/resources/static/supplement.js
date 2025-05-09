async function loadSupplements() {
  const sort = document.getElementById('list-sort').value;
  const res = await fetch(`/api/supplements?sort=${sort}&direction=asc`);
  const allSupplements = await res.json();
  renderSupplements(allSupplements);
}

function renderSupplements(list) {
  const tbody = document.getElementById('supplement-body');
  tbody.innerHTML = '';
  const isFolded = document.getElementById('toggle-fold')?.classList.contains('folded');
  const shown = isFolded ? list.slice(0, 5) : list;
  const editMode = document.getElementById('toggle-change')?.classList.contains('execute');
  const deleteMode = document.getElementById('toggle-delete')?.classList.contains('execute');

  shown.forEach(item => {
    const row = document.createElement('tr');
    const types = item.types?.map(e => e.name).join(', ') || '';
    const effects = item.effects?.map(e => `<span class="${e.grade}">${e.effectTag?.name} (${e.grade ?? '-'})</span>`).join(', ') || '';
    const sideEffects = item.sideEffects?.map(e => `<span class="${e.grade}">${e.sideEffectTag?.name} (${e.grade ?? '-'})</span>`).join(', ') || '';
    const link = `http://localhost:8080/detail.html?id=${item.id}`;

    row.innerHTML = `
      <td contenteditable="${editMode}" data-field="korName">
      <a href = ${link}>${item.korName}</a>
      </td>
      <td contenteditable="${editMode}" data-field="engName">
      <a href = ${link}>${item.engName}</a>
      </td>
      <td>${types}</td>
      <td contenteditable="${editMode}" data-field="dosage">${item.dosage}</td>
      <td contenteditable="${editMode}" data-field="cost">${item.cost}</td>
      <td>${effects}</td>
      <td>${sideEffects}</td>
      ${editMode ? `<td>
      <div class="edit-group">
      <button class="modal-btn" data-id="${item.id}">태그</button>
      <button class="save-btn" data-id="${item.id}">저장</button>
      </div>
      </td>` : ''}
    `;

    row.addEventListener('click', async (e) => {
       if (deleteMode) {
              e.preventDefault(); // 링크 이동 방지
                if (confirm(`'${item.korName}' 을 삭제할까요?`)) {
                  await fetch(`/api/supplements/${item.id}`, { method: 'DELETE' });
                  await loadSupplements();
                }
            return;
      }
      else if (e.target.classList.contains('save-btn')) {

      const typeIds= item.types?.map(t => t.id) || [];
      const effectGrades= item.effects?.map(e => ({
        effectId: e.effectTag.id,
        grade: e.grade
      })) || [];
      const sideEffectGrades= item.sideEffects?.map(e => ({
        sideEffectId: e.sideEffectTag.id,
        grade: e.grade
      })) || [];

        const updated = {
          id: item.id,
          korName: row.querySelector('[data-field="korName"]').textContent.trim(),
          engName: row.querySelector('[data-field="engName"]').textContent.trim(),
          dosage: row.querySelector('[data-field="dosage"]').textContent.trim(),
          cost: parseFloat(row.querySelector('[data-field="cost"]').textContent.trim()),
          typeIds,
          effectGrades,
          sideEffectGrades
        };

        const res = await fetch(`/api/supplements/${item.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
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
      else if(e.target.classList.contains('modal-btn')){
            document.querySelectorAll('#type-checkboxes input').forEach(cb => {
                 cb.checked = item.types?.some(e => e.id == cb.value);
               });
           document.querySelectorAll('#positive-checkboxes div').forEach(wrapper => {
             const checkbox = wrapper.querySelector('input[type="checkbox"]');
             const select = wrapper.querySelector('select');

             const matched = item.effects?.find(e => e.effectTag?.id == checkbox.value);

             if (matched) {
               checkbox.checked = true;
               select.value = matched.grade ?? 'null';  // grade가 없으면 'null' 기본값
             } else {
               checkbox.checked = false;
               select.value = 'null';
             }
           });

            document.querySelectorAll('#negative-checkboxes div').forEach(wrapper => {
            const checkbox = wrapper.querySelector('input[type="checkbox"]');
            const select = wrapper.querySelector('select');

              const matched = item.sideEffects?.find(e => e.sideEffectTag?.id == checkbox.value);

              if (matched) {
                checkbox.checked = true;
                select.value = matched.grade ?? 'null';  // grade가 없으면 'null' 기본값
              } else {
                checkbox.checked = false;
                select.value = 'null';
              }
            });
           document.getElementById('modal').style.display = 'block';
                 const closeButton = document.getElementById('close-modal');
                 const existing = document.getElementById('modal-save-btn');
                 if (existing) existing.remove();
                 const saveButton = document.createElement('button');
                 saveButton.id = 'modal-save-btn';
                 saveButton.textContent = '저장';
                 closeButton.after(saveButton);

      closeButton.addEventListener('click', e=>{
          saveButton.remove();
      });
        //버튼 누르면 저장
      saveButton.addEventListener('click',async e=>{
      const typeIds = Array.from(document.querySelectorAll('#type-checkboxes input:checked')).map(cb => parseInt(cb.value));
            const effectGrades = Array.from(document.querySelectorAll('#positive-checkboxes div')).flatMap(wrapper => {
                  const checkbox = wrapper.querySelector('input[type="checkbox"]');
                  const select = wrapper.querySelector('select');

                  if (checkbox.checked) {
                    return [{
                      effectId: parseInt(checkbox.value),
                      grade: select?.value === 'null' ? null : select?.value
                    }];
                  }
                  return [];
                });
            const sideEffectGrades = Array.from(document.querySelectorAll('#negative-checkboxes div')).flatMap(wrapper => {
                      const checkbox = wrapper.querySelector('input[type="checkbox"]');
                      const select = wrapper.querySelector('select');

                      if (checkbox.checked) {
                        return [{
                          sideEffectId: parseInt(checkbox.value),
                          grade: select?.value === 'null' ? null : select?.value
                        }];
                      }
                      return [];
               });
                       const updated = {
                         id: item.id,
                         korName: item.korName,
                         engName: item.engName,
                         dosage: item.dosage,
                         cost: item.cost,
                         typeIds,
                         effectGrades,
                         sideEffectGrades
                       };

                       const res = await fetch(`/api/supplements/${item.id}`, {
                         method: 'PUT',
                         headers: { 'Content-Type': 'application/json' },
                         body: JSON.stringify(updated)
                       });
                         if (res.ok) {
                                 alert('저장되었습니다');
                                 await loadSupplements();
                                 document.getElementById('list-sort')?.dispatchEvent(new Event('change'));
                               } else {
                                 alert('저장 실패');
                               }
      });


      }
   });

    tbody.appendChild(row);
  });
}

// 접기 버튼
document.getElementById('toggle-fold')?.addEventListener('click', e => {
  e.target.classList.toggle('folded');
  document.getElementById('toggle-fold').textContent = e.target.classList.contains('folded') ? '펼치기' : '접기';
  loadSupplements();
});

document.getElementById('tag-toggle-fold').addEventListener('click' , e=>{
   e.target.classList.toggle('folded');
   document.getElementById('tag-toggle-fold').textContent = e.target.classList.contains('folded') ? '펼치기' : '접기';
   loadTags();
});
// 정렬
document.getElementById('list-sort')?.addEventListener('change', () => {
  loadSupplements();
});

document.getElementById('tag-sort')?.addEventListener('change', () => {
  loadTags();
});

document.getElementById('modal-sort')?.addEventListener('change', () => {
  loadTags();
});

// 삭제/수정 토글
document.getElementById('toggle-delete')?.addEventListener('click', e => {
 const editBtn = document.getElementById('toggle-change');
   e.target.classList.toggle('execute');
   e.target.textContent = e.target.classList.contains('execute') ? "삭제중" : "삭제";
   editBtn.classList.remove('execute');
   editBtn.textContent = "수정";
   loadSupplements();
});

document.getElementById('toggle-change')?.addEventListener('click', e => {
const deleteBtn = document.getElementById('toggle-delete');
  e.target.classList.toggle('execute');
  e.target.textContent = e.target.classList.contains('execute') ? "수정중" : "수정";
  deleteBtn.classList.remove('execute');
  deleteBtn.textContent = "삭제";
  loadSupplements();
});

// 태그 로딩
async function loadTags() {
  const sort = document.getElementById('tag-sort').value;
  const allTypes = ['type','positive', 'negative'];

  for (let type of allTypes) {
    const res = await fetch(`/api/tags?type=${type}&sort=${encodeURIComponent(sort)}&direction=asc`);
    const list = await res.json();
    renderTags(type, list);
    renderModal(type, list);
  }
}

function renderTags(type, list) {
  let tagList;
  const deleteMode = document.getElementById('toggle-delete')?.classList.contains('execute');
  switch (type) {
    case "type":
      tagList = document.getElementById('type-tag-list');
      break;
    case "positive":
      tagList = document.getElementById('positive-tag-list');
      break;
    case "negative":
      tagList = document.getElementById('negative-tag-list');
      break;
    default:
      console.error("❌ 알 수 없는 type:", type);
      return;
  }
  tagList.innerHTML = '';
  const isFolded = document.getElementById('tag-toggle-fold')?.classList.contains('folded');
  const shown = isFolded ? list.slice(0, 5) : list;
  shown.forEach(tag=>{
     const li = document.createElement('li');
     li.textContent = tag.name;
     li.dataset.id = tag.id;
     li.dataset.type = tag.type;
     li.style.cursor = 'pointer';
     tagList.appendChild(li);
  });
     tagList.replaceWith(tagList.cloneNode(true)); // 기존 이벤트 제거
     tagList = document.getElementById(tagList.id); // 새로 참조
     tagList.addEventListener('click', async e => {
       if (e.target.tagName === 'LI') {
           if (deleteMode) {
               if (confirm(`'${e.target.textContent}' 을 삭제할까요?`)) {
                   await fetch(`/api/tags/${e.target.dataset.type}/${e.target.dataset.id}`, { method: 'DELETE' });
                   await loadTags();
               }
               return;
           }
           else{
                e.target.classList.toggle('selected');
                filterByTag();
           }
       }
     });
}
// 모달 랜더링

const GRADE_COLOR_MAP = {
  A: 'blue',
  B: 'red',
  C: 'lime',
  D: 'gray'
};

function createCheckbox(tagId, prefix) {
  const cb = document.createElement('input');
  cb.type = 'checkbox';
  cb.value = tagId;
  cb.id = `${prefix}-${tagId}`;
  return cb;
}

function createLabel(forId, text) {
  const lbl = document.createElement('label');
  lbl.htmlFor = forId;
  lbl.textContent = text;
  return lbl;
}

function createGradeSelectBox(tagId, prefix) {
  const gr = document.createElement('select');
  gr.name = `grade-${tagId}`;
  gr.id = `${prefix}-grade-${tagId}`;
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
    const selected = gr.value;
    gr.style.color = GRADE_COLOR_MAP[selected] || 'gray';
  });

  return gr;
}

function createModalInner(tag, type) {
  const wrapper = document.createElement('div');
  wrapper.classList.add('modal-inner');

  const cb = createCheckbox(tag.id, type);
  const lbl = createLabel(cb.id, tag.name);
  wrapper.append(lbl, cb);

  if (type === 'positive' || type === 'negative') {
    const gr = createGradeSelectBox(tag.id, type);
    wrapper.append(gr);
  }

  return wrapper;
}

function renderModal(type, list) {
  let container;

  switch (type) {
    case 'type':
      container = document.getElementById('type-checkboxes');
      break;
    case 'positive':
      container = document.getElementById('positive-checkboxes');
      break;
    case 'negative':
      container = document.getElementById('negative-checkboxes');
      break;
    default:
      console.warn(`❌ 알 수 없는 type: ${type}`);
      return;
  }

  container.innerHTML = '';
  list.forEach(tag => {
    const item = createModalInner(tag, type);
    container.appendChild(item);
  });
}


// 태그 필터링
async function filterByTag() {
  const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
  const sort = document.getElementById('list-sort').value;

  if (selected.length === 0) {
      loadSupplements();
  } else {
    const typeIds = selected.filter(e => e.dataset.type === 'type').map(e=>e.dataset.id);
    const effectIds = selected.filter(e => e.dataset.type === 'positive').map(e=>e.dataset.id);
    const sideEffectIds = selected.filter(e => e.dataset.type === 'negative').map(e=>e.dataset.id);

    const params = new URLSearchParams();
    typeIds.forEach(id => params.append('typeIds', id));
    effectIds.forEach(id => params.append('effectIds', id));
    sideEffectIds.forEach(id => params.append('sideEffectIds', id));

   const res = await fetch(`/api/supplements/filter?${params.toString()}&sort=${sort}&direction=asc`);
   const filtered = await res.json();

    renderSupplements(filtered);
  }
}

//검색 폼
document.getElementById('search-form').addEventListener('submit', async e => {
  e.preventDefault();  // 폼 제출 막기

  const keyword = e.target.input.value;
  const sort = document.getElementById('list-sort').value;

  const res = await fetch(`/api/supplements/search?keyword=${encodeURIComponent(keyword)}&sort=${sort}&direction=asc`);
  const filtered = await res.json();
  renderSupplements(filtered);
});


document.getElementById('tag-search-form').addEventListener('submit',async e=>{
   e.preventDefault();  // 폼 제출 막기

   const keyword = e.target.input.value;
   const sort = document.getElementById('tag-sort').value;
   const allTypes = ['type', 'positive', 'negative'];
   for(let type of allTypes){
   const res = await fetch(`/api/tags/search?keyword=${encodeURIComponent(keyword)}&type=${type}&sort=${sort}&direction=asc`);
   const filtered = await res.json();
   renderTags(type,filtered);
   }
});

document.getElementById('modal-search-form').addEventListener('submit',async e=>{
     e.preventDefault();  // 폼 제출 막기

       const keyword = e.target.input.value;
       const sort = document.getElementById('modal-sort').value;
       const allTypes = ['type', 'positive', 'negative'];
       for(let type of allTypes){
       const res = await fetch(`/api/tags/search?keyword=${encodeURIComponent(keyword)}&type=${type}&sort=${sort}&direction=asc`);
       const filtered = await res.json();
       renderModal(type,filtered);
       }
});

// 성분 추가
document.getElementById('insert-form').addEventListener('submit', async e => {
  e.preventDefault();
  const form = e.target;

  const typeIds = Array.from(document.querySelectorAll('#type-checkboxes input:checked')).map(cb => parseInt(cb.value));
  const effectGrades = Array.from(document.querySelectorAll('#positive-checkboxes div')).flatMap(wrapper => {
  const checkbox = wrapper.querySelector('input[type="checkbox"]');
  const select = wrapper.querySelector('select');

  if (checkbox.checked) {
    return [{
      effectId: parseInt(checkbox.value),
       grade: select?.value === 'null' ? null : select?.value
    }];
  }
  return [];
});

  const sideEffectGrades = Array.from(document.querySelectorAll('#negative-checkboxes div')).flatMap(wrapper => {
  const checkbox = wrapper.querySelector('input[type="checkbox"]');
  const select = wrapper.querySelector('select');

  if (checkbox.checked) {
    return [{
      sideEffectId: parseInt(checkbox.value),
       grade: select?.value === 'null' ? null : select?.value
    }];
  }
  return [];
});


  const data = {
    korName: form.korName.value,
    engName: form.engName.value,
    typeIds,
    dosage: form.dosage.value,
    cost: parseFloat(form.cost.value),
    effectGrades,
    sideEffectGrades
  };

  const res = await fetch('/api/supplements', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
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
      select.value = 'null';  // 기본 옵션으로 되돌림
    });
      document.querySelectorAll('#negative-checkboxes div').forEach(wrapper => {
          const checkbox = wrapper.querySelector('input[type="checkbox"]');
          const select = wrapper.querySelector('select');

          checkbox.checked = false;
          select.value = 'null';  // 기본 옵션으로 되돌림
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
    headers: { 'Content-Type': 'application/json' },
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

// 모달 열고 닫기

document.getElementById('open-modal')?.addEventListener('click', e => {
  document.getElementById('modal').style.display = 'block';
});
document.getElementById('close-modal')?.addEventListener('click', e => {
  document.getElementById('modal').style.display = 'none';
});


// 초기 로딩
document.addEventListener('DOMContentLoaded', async e => {
  const sortSelect = document.getElementById('list-sort');
  if (sortSelect) {
    sortSelect.value = 'engName';
    sortSelect.dispatchEvent(new Event('change'));
  }
  const sortTag = document.getElementById('tag-sort');
  if (sortTag) {
    sortTag.value = 'name';
    sortTag.dispatchEvent(new Event('change'));
  }
  const sortModal = document.getElementById('modal-sort');
  if (sortModal) {
    sortModal.value = 'name';
    sortModal.dispatchEvent(new Event('change'));
  }
    await loadSupplements();
    await loadTags();

    fetch('/nav.html')
      .then(res => res.text())
      .then(html => {
        document.getElementById('navbar').innerHTML = html;
      });
});
