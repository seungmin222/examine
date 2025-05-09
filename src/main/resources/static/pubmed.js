async function loadPubmeds() {
  const sort = document.getElementById('list-sort').value;
  const res = await fetch(`/api/pubmeds?sort=${sort}&direction=asc`);
  allPubmeds = await res.json();
  renderPubmeds(allPubmeds);
}

function renderPubmeds(list) {
  const tbody = document.getElementById('pubmed-body');
  tbody.innerHTML = '';
  const folded = document.getElementById('fold-toggle')?.classList.contains('folded');
  const shown = folded ? list.slice(0, 5) : list;
  const editMode = document.getElementById('toggle-change')?.classList.contains('execute');
  const deleteMode = document.getElementById('toggle-delete')?.classList.contains('execute');

  shown.forEach(item => {
    const row = document.createElement('tr');
    const supplements = item.supplements?.map(e => e.korName).join(', ') || '';
    const effects = item.effects?.map(e => e.name).join(', ') || '';
    const sideEffects = item.sideEffects?.map(e => e.name).join(', ') || '';
    row.innerHTML = `
      <td contenteditable="${editMode}" data-field="title">
      <span class="tooltip" data-tooltip="${item.summary}">
      <a href="${item.link}"> ${item.title}</a>
      </span>
      </td>
      <td>${item.trial_design?.name || ''}</td>
      <td contenteditable="${editMode}" data-field="trial_length">${item.trial_length}</td>
      <td contenteditable="${editMode}" data-field="participants">${item.participants}</td>
      <td>${supplements}</td>
      <td>${effects}</td>
      <td>${sideEffects}</td>
      ${editMode ? `<td>
         <div class="edit-group">
            <button class="edit-btn" data-id="${item.id}">세부</button>
            <button class="modal-btn" data-id="${item.id}">태그</button>
            <button class="save-btn" data-id="${item.id}">저장</button>
         </div>
      </td>`: ''}
      `;

    row.addEventListener('click', async e => {
    if (deleteMode) {
      if (e.target.classList.contains('pubmed-link')) {
          e.preventDefault(); // 링크 이동 방지
            if (confirm(`'${item.title}' 논문을 삭제할까요?`)) {
              await fetch(`/api/pubmeds/${item.id}`, { method: 'DELETE' });
              await loadPubmeds();
            }
        return;
      }
    }
    else if (e.target.classList.contains('save-btn')) {
        const supplements = item.supplements?.map(e => e.id);
        const effects = item.effects?.map(e => e.id);
        const sideEffects = item.sideEffects?.map(e => e.id);
      const updated = {
           title:row.querySelector('[data-field="title"]').textContent.trim(),
           link: item.link,
           trial_design_id: item.trial_design.id,
           trial_length: row.querySelector('[data-field="trial_length"]').textContent.trim(),
           participants: row.querySelector('[data-field="participants"]').textContent.trim(),
           supplements,
           effects,
           sideEffects,
           summary: item.summary
         };

         const res = await fetch(`/api/pubmeds/${item.id}`, {
           method: 'PUT',
           headers: { 'Content-Type': 'application/json' },
           body: JSON.stringify(updated)
         });

         if (res.ok) {
           alert("논문이 수정되었습니다.");
           form.reset();
           await loadPubmeds();
         } else {
           alert("수정 실패");
         }
    }
    else if (e.target.classList.contains('modal-btn')) {
     document.querySelectorAll('#supplement-checkboxes input').forEach(cb => {
        cb.checked = item.supplements?.some(e => e.id == cb.value);
      });
      document.querySelectorAll('#positive-checkboxes input').forEach(cb => {
        cb.checked = item.effects?.some(e => e.id == cb.value);
      });
      document.querySelectorAll('#negative-checkboxes input').forEach(cb => {
        cb.checked = item.sideEffects?.some(e => e.id == cb.value);
      });
     document.getElementById('modal').style.display = 'block';
          const closeButton = document.getElementById('close-modal');
          const saveButton = document.createElement('button');
          saveButton.textContent = '저장';
          closeButton.after(saveButton);
          closeButton.addEventListener('click', e=>{
          saveButton.remove();
          });
               saveButton.addEventListener( 'click', async e=>{
    const supplements = Array.from(document.querySelectorAll('#supplement-checkboxes input:checked')).map(cb => parseInt(cb.value));
    const effects = Array.from(document.querySelectorAll('#positive-checkboxes input:checked')).map(cb => parseInt(cb.value));
    const sideEffects = Array.from(document.querySelectorAll('#negative-checkboxes input:checked')).map(cb => parseInt(cb.value));
    const updated = {
          title: item.title,
          link: item.link,
          trial_design_id: item.trial_design.id,
          trial_length: item.trial_length,
          participants: item.participants,
          supplements,
          effects,
          sideEffects,
          summary: item.summary
        };

        const res = await fetch(`/api/pubmeds/${item.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updated)
        });

        if (res.ok) {
          alert("논문이 수정되었습니다.");
          form.reset();
          await loadPubmeds();
        } else {
          alert("수정 실패");
        }
    });
    }
    else if (e.target.classList.contains('edit-btn')) {
        document.getElementById('edit-modal').style.display = 'block';
        const closeButton = document.getElementById('close-edit-modal');
        const saveButton = document.createElement('button');
        saveButton.textContent = '저장';
        closeButton.after(saveButton);
       closeButton.addEventListener('click', e=>{
              saveButton.remove();
       });
       saveButton.addEventListener('click',async e => {
        const supplements = item.supplements?.map(e => e.id);
        const effects = item.effects?.map(e => e.id);
        const sideEffects = item.sideEffects?.map(e => e.id);

        const form = document.getElementById('edit-form');
        const link = form.link.value;
        const trial_design_id = parseInt(form.trial_design.value);
        const summary = form.summary.value;

        const updated = {
              title: item.title,
              link,
              trial_design_id,
              trial_length: item.trial_length,
              participants: item.participants,
              supplements,
              effects,
              sideEffects,
              summary
            };

            const res = await fetch(`/api/pubmeds/${item.id}`, {
              method: 'PUT',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(updated)
            });

            if (res.ok) {
              alert("논문이 수정되었습니다.");
              form.reset();
              await loadPubmeds();
            } else {
              alert("수정 실패");
            }
        });
    }
    });

    tbody.appendChild(row);
  });
}

//검색 폼
document.getElementById('search-form').addEventListener('submit', async e => {
  e.preventDefault();  // 폼 제출 막기

  const keyword = e.target.input.value;
  const sort = document.getElementById('list-sort').value;

  const res = await fetch(`/api/pubmeds/search?keyword=${encodeURIComponent(keyword)}&sort=${sort}&direction=asc`);
  const filtered = await res.json();
  renderPubmeds(filtered);
});

document.getElementById('tag-search-form').addEventListener('submit',async e=>{
      e.preventDefault();  // 폼 제출 막기

      const keyword = e.target.input.value;
      const sort = document.getElementById('tag-sort').value;
      const allTypes = ['trialDesign','supplement','positive','negative'];
      for(let type of allTypes){
         const res = await fetch(`/api/tags/search?keyword=${encodeURIComponent(keyword)}&type=${type}&sort=${sort}&direction=asc`);
         const filtered = await res.json();
         renderTags(type,filtered);
      }
});

document.getElementById('modal-search-form').addEventListener('submit',async e=>{
    e.preventDefault();  // 폼 제출 막기

          const keyword = e.target.input.value;
          const sort = document.getElementById('tag-sort').value;
          const allTypes = ['trialDesign','supplement','positive','negative'];
          for(let type of allTypes){
             const res = await fetch(`/api/tags/search?keyword=${encodeURIComponent(keyword)}&type=${type}&sort=${sort}&direction=asc`);
             const filtered = await res.json();
             renderModal(type,filtered);
          }
});

// 정렬
document.getElementById('list-sort')?.addEventListener('change', () => {
  loadPubmeds();
});


document.getElementById('tag-sort')?.addEventListener('change', e => {
  loadTags();
});

// 접기 버튼
document.getElementById('toggle-fold')?.addEventListener('click', e => {
  e.target.classList.toggle('folded');
  document.getElementById('toggle-fold').textContent = e.target.classList.contains('folded') ? '펼치기' : '접기';
  loadPubmeds();
});

document.getElementById('tag-toggle-fold').addEventListener('click' , e=>{
   e.target.classList.toggle('folded');
   document.getElementById('tag-toggle-fold').textContent = e.target.classList.contains('folded') ? '펼치기' : '접기';
   loadTags();
});


// 정렬
document.getElementById('list-sort')?.addEventListener('change', () => {
  loadPubmeds();
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
   e.target.textContent = e.target.classList.contains('execute') ? "삭제중" : "삭제";
   e.target.classList.toggle('execute');
   editBtn.textContent = "수정";
   editBtn.classList.remove('execute');
   loadPubmeds();
});

document.getElementById('toggle-change')?.addEventListener('click', e => {
const deleteBtn = document.getElementById('toggle-delete');
  e.target.textContent = e.target.classList.contains('execute') ? "수정중" : "수정";
  e.target.classList.toggle('execute');
  deleteBtn.textContent = "삭제";
  deleteBtn.classList.remove('execute');
  loadPubmeds();
});
// 태그 로딩
async function loadTags() {
  const sort = document.getElementById('tag-sort').value;
  const allTypes = ['positive', 'negative', 'supplement', 'trialDesign'];

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
    case "trialDesign":
      tagList = document.getElementById('trial-design-list');
      break;
    case "supplement":
      tagList = document.getElementById('supplement-list');
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
  const folded = document.getElementById('tag-toggle-fold')?.classList.contains('folded');
  const shown = folded ? list.slice(0, 5) : list;
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

function renderModal(type, list) {
  const sort = document.getElementById('modal-sort').value;

  switch (type) {
    case 'trialDesign': {
      const trial_designSelect = document.getElementById('trial-design-select');
      const trial_designEdit = document.getElementById('trial-design-edit');
      trial_designSelect.innerHTML = '<option disabled selected>분류</option>';
      trial_designEdit.innerHTML = '<option disabled selected>분류</option>';

      list.forEach(tag => {
        const opt1 = document.createElement('option');
        opt1.value = tag.id;
        opt1.textContent = tag.name;

        const opt2 = document.createElement('option');
        opt2.value = tag.id;
        opt2.textContent = tag.name;

        trial_designSelect.appendChild(opt1);
        trial_designEdit.appendChild(opt2);
      });
      break;
    }

    case 'supplement': {
      const supCheckboxes = document.getElementById('supplement-checkboxes');
      supCheckboxes.innerHTML = '';

      list.forEach(tag => {
        const wrapper = document.createElement('div');
        wrapper.classList.add('modal-inner');

        const cb = document.createElement('input');
        cb.type = 'checkbox';
        cb.value = tag.id;
        cb.id = `sup-${tag.id}`;

        const lbl = document.createElement('label');
        lbl.htmlFor = cb.id;
        lbl.textContent = tag.name;

        wrapper.append(lbl, cb);
        supCheckboxes.append(wrapper);
      });
      break;
    }

    case 'positive': {
      const posCheckboxes = document.getElementById('positive-checkboxes');
      posCheckboxes.innerHTML = '';

      list.forEach(tag => {
        const wrapper = document.createElement('div');
        wrapper.classList.add('modal-inner');

        const cb = document.createElement('input');
        cb.type = 'checkbox';
        cb.value = tag.id;
        cb.id = `pos-${tag.id}`;

        const lbl = document.createElement('label');
        lbl.htmlFor = cb.id;
        lbl.textContent = tag.name;

        wrapper.append(lbl, cb);
        posCheckboxes.append(wrapper);
      });
      break;
    }

    case 'negative': {
      const negCheckboxes = document.getElementById('negative-checkboxes');
      negCheckboxes.innerHTML = '';

      list.forEach(tag => {
        const wrapper = document.createElement('div');
        wrapper.classList.add('modal-inner');

        const cb = document.createElement('input');
        cb.type = 'checkbox';
        cb.value = tag.id;
        cb.id = `neg-${tag.id}`;

        const lbl = document.createElement('label');
        lbl.htmlFor = cb.id;
        lbl.textContent = tag.name;

        wrapper.append(lbl, cb);
        negCheckboxes.append(wrapper);
      });
      break;
    }

    default:
      console.warn(`지원하지 않는 type: ${type}`);
  }
}


// 태그 필터링
async function filterByTag() {
  const selected = Array.from(document.querySelectorAll('li[data-type].selected'));
  const sort = document.getElementById('list-sort').value;

  if (selected.length === 0) {
      loadPubmeds();
  } else {
    const trialDesign = selected.filter(e => e.dataset.type === 'trialDesign').map(e=>e.dataset.id);
    const supplementIds = selected.filter(e => e.dataset.type === 'supplement').map(e=>e.dataset.id);
    const effectIds = selected.filter(e => e.dataset.type === 'positive').map(e=>e.dataset.id);
    const sideEffectIds = selected.filter(e => e.dataset.type === 'negative').map(e=>e.dataset.id);
    const params = new URLSearchParams();
    trialDesign.forEach(id => params.append('trialDesign', id));
    supplementIds.forEach(id => params.append('supplementIds', id));
    effectIds.forEach(id => params.append('effectIds', id));
    sideEffectIds.forEach(id => params.append('sideEffectIds', id));

   const res = await fetch(`/api/pubmeds/filter?${params.toString()}&sort=${sort}&direction=asc`);
   const filtered = await res.json();

    renderPubmeds(filtered);
  }
}

// 논문 추가
document.getElementById('insert-form').addEventListener('submit', async e => {
  e.preventDefault();
  const form = e.target;

  const trial_design_id = parseInt(form.trial_design.value);
  const supplements = Array.from(document.querySelectorAll('#supplement-checkboxes input:checked')).map(cb => parseInt(cb.value));
  const effects = Array.from(document.querySelectorAll('#positive-checkboxes input:checked')).map(cb => parseInt(cb.value));
  const sideEffects = Array.from(document.querySelectorAll('#negative-checkboxes input:checked')).map(cb => parseInt(cb.value));

  const data = {
    title: form.title.value,
    link: form.link.value,
    trial_design_id,
    trial_length: form.trial_length.value,
    participants: form.participants.value,
    supplements,
    effects,
    sideEffects,
    summary: form.summary.value
  };

  const res = await fetch('/api/pubmeds', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });

  if (res.ok) {
    alert("논문이 추가되었습니다.");
    form.reset();
    await loadPubmeds();
  } else {
    alert("추가 실패");
  }
});

// 모달 열고 닫기
document.getElementById('open-modal')?.addEventListener('click', () => {
  document.getElementById('modal').style.display = 'block';
});
document.getElementById('close-modal')?.addEventListener('click', () => {
  document.getElementById('modal').style.display = 'none';
});
document.getElementById('close-edit-modal')?.addEventListener('click', () => {
  document.getElementById('edit-modal').style.display = 'none';
});


// 초기 로딩
document.addEventListener('DOMContentLoaded', async () => {
  await loadPubmeds();
  await loadTags();

 fetch('/nav.html')
    .then(res => res.text())
    .then(html => {
      document.getElementById('navbar').innerHTML = html;
    });


  const sortSelect = document.getElementById('list-sort');


  if (sortSelect) {
    sortSelect.value = 'title';
    sortSelect.dispatchEvent(new Event('change'));
  }
   const tagSelect = document.getElementById('tag-sort');
      if (tagSelect) {
        tagSelect.value = 'name';
        tagSelect.dispatchEvent(new Event('change'));
   }
   const sortModal = document.getElementById('modal-sort');
     if (sortModal) {
       sortModal.value = 'name';
       sortModal.dispatchEvent(new Event('change'));
   }
});
