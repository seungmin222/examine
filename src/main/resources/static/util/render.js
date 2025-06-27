import{
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
} from '/util/utils.js';

function renderSupplements(list,supplementMap) {
    const tbody = document.getElementById('supplement-body');
    tbody.innerHTML = '';
    supplementMap.clear();
    const isFolded = document.getElementById('toggle-fold')?.classList.contains('folded');
    const shown = isFolded ? list.slice(0, 5) : list;
    const editMode = document.getElementById('toggle-change')?.classList.contains('execute');

    shown.forEach(item => {
        supplementMap.set(item.id, item);
        const row = document.createElement('tr');
        row.dataset.id = item.id;
        const types = item.types?.map(e => e.name).join(', ') || '';
        const effects = item.effects?.map(e => `<span class="${e.tier}">${e.effectName} (${e.tier ?? '-'})</span>`).join(', ') || '';
        const sideEffects = item.sideEffects?.map(e => `<span class="${e.tier}">${e.sideEffectName} (${e.tier ?? '-'})</span>`).join(', ') || '';
        const link = `http://localhost:8080/detail.html?id=${item.id}`;

        row.innerHTML = `
      <td>
      ${editMode
      ? `<input name="korName" value="${item.korName}" class="w-32"/>`
      : `<a href = ${link}>${item.korName}</a>`
      }
      </td>
      <td>
      ${editMode
      ? `<input name="engName" value="${item.engName}" class="w-32"/>`
      : `<a href = ${link}>${item.engName}</a>`
      }
      </td>
      <td>${types}</td>
      <td>
      ${editMode
      ? `<input name="dosageValue" value="${item.dosageValue}"/>
         <select name="dosageUnit" class="w-16">
            <option value="g" ${item.dosageUnit === 'g' ? 'selected' : ''}>g</option>
            <option value="mg" ${item.dosageUnit === 'mg' ? 'selected' : ''}>mg</option>
            <option value="ug" ${item.dosageUnit === 'ug' ? 'selected' : ''}>ug</option>
            <option value="iu" ${item.dosageUnit === 'iu' ? 'selected' : ''}>iu</option>
         </select>`
      : `${item.dosageValue}${item.dosageUnit}`
      }
      </td>
      <td>
       ${editMode
       ? `<input type="number" step="0.01" name="cost" value="${item.cost}"/>`
       : item.cost
       }
       </td>
      <td>${effects}</td>
      <td>${sideEffects}</td>
      ${editMode ? `<td>
      <div class="flex flex-col gap-2">
      <button class="modal-btn">태그</button>
      <button class="save-btn">저장</button>
      </div>
      </td>` : ''}
    `;
        tbody.appendChild(row);
    });
}

function renderJournals(list, journalMap) {
    const tbody = document.getElementById('journal-body');
    tbody.innerHTML = '';
    journalMap.clear();
    const folded = document.getElementById('fold-toggle')?.classList.contains('folded');
    const shown = folded ? list.slice(0, 5) : list;
    const editMode = document.getElementById('toggle-change')?.classList.contains('execute');

    const trialDesignOptions = [ // 추후 하드코딩 말고 api로 받아서 전역 map으로 설정하기
      { value: '1', label: 'Meta-analysis', tier: 'A' },
      { value: '20', label: 'Systematic Review', tier: 'A' },
      { value: '21', label: 'RCT', tier: 'B' },
      { value: '22', label: 'Non-RCT', tier: 'B' },
      { value: '23', label: 'Cohort', tier: 'C' },
      { value: '24', label: 'Case-control', tier: 'C' },
      { value: '25', label: 'Cross-sectional', tier: 'C' },
      { value: '26', label: 'Case Report', tier: 'D' },
      { value: '27', label: 'Animal Study', tier: 'F' },
      { value: '28', label: 'In-vitro Study', tier: 'F' },
      { value: '29', label: 'Unknown', tier: 'F' }
    ];

    shown.forEach(item => {
        journalMap.set(item.id, item);
        const row = document.createElement('tr');
        row.dataset.id = item.id; // ✅ 여기 추가

        const supplements = [...new Set([
          ...(item.effects?.map(e => e.supplementName) || []),
          ...(item.sideEffects?.map(e => e.supplementName) || [])
        ])].join(', ') || '';
        const effects = [...new Set(item.effects?.map(e => e.effectName) || [])].join(', ') || '';
        const sideEffects = [...new Set(item.sideEffects?.map(e => e.sideEffectName) || [])].join(', ') || '';
        let blindClass = ""
        if (item.blind === 'double-blind') {
            blindClass = 'A';
        }
        else if (item.blind === 'single-blind') {
            blindClass = 'B';
        }

        const parallel = item.parallel ? 'O' : 'X';
        const trialDesignSelectOptions = trialDesignOptions.map(opt => `
              <option value="${opt.value}" class="w-32 ${opt.tier}" ${item.trialDesign?.id === Number(opt.value) ? 'selected' : ''}>
                ${opt.label}
              </option>
            `).join('');

        row.innerHTML = `
          <td id="tooltip-${item.id}">
              <a target="_blank" rel="noopener noreferrer" href="${item.link}"> ${item.title}</a>
          </td>
          <td class="${item.trialDesign?.tier[0]}">
            ${editMode
               ? `<select name="trialDesign" class="w-32 ${item.trialDesign?.tier[0]}">
                      ${trialDesignSelectOptions}
                   </select>`
               : `${item.trialDesign?.name}`
             }
          </td>
          <td>
            ${editMode
               ? `<select name="blind" class="w-32 ${blindClass}">
                     <option value="0" class='w-32' ${item.blind === 'open-label' ? 'selected' : ''}>open-label</option>
                     <option value="1" class="w-32 B" ${item.blind === 'single-blind' ? 'selected' : ''}>single-blind</option>
                     <option value="2" class="w-32 A" ${item.blind === 'double-blind' ? 'selected' : ''}>double-blind</option>
                  </select>`
               : `${item.blind}`
             }
          </td>
          <td>
             ${editMode
                ? `<select name="parallel" class="w-20">
                      <option value="true" ${item.parallel ? 'selected' : ''}>O</option>
                      <option value="false" ${!item.parallel ? 'selected' : ''}>X</option>
                   </select>`
                : `${parallel}`
              }
          </td>
          <td>
            ${editMode
              ? `<input name="durationValue" type="number" value="${item.duration.value}"/>
                 <select name="durationUnit">
                    <option value="day" ${item.duration.unit === 'day' ? 'selected' : ''}>day</option>
                    <option value="week" ${item.duration.unit === 'week' ? 'selected' : ''}>week</option>
                    <option value="month" ${item.duration.unit === 'month' ? 'selected' : ''}>month</option>
                    <option value="year" ${item.duration.unit === 'year' ? 'selected' : ''}>year</option>
                 </select>`
              : `${item.duration.value} ${item.duration.unit}`
            }
          </td>
          <td>
            ${editMode
              ? `<input type="number" value="${item.participants}" class="w-24" name="participants"/>`
              : item.participants}
          </td>
          <td>${supplements}</td>
          <td>${effects}</td>
          <td>${sideEffects}</td>
          <td>${item.date?.slice(0, 7)}</td>
          ${editMode ? `<td>
             <div class="flex flex-col gap-2">
                <button class="modal-btn" data-id="${item.id}">태그</button>
                <button class="save-btn" data-id="${item.id}">저장</button>
             </div>
          </td>` : ''}
        `;

        tbody.appendChild(row);
        requestAnimationFrame(() => {
          createTooltip(`tooltip-${item.id}`, item.summary, 'right' ,'w-1/3');
        });
    });
}

function renderTags(type, list) {
    let tagList = document.getElementById(`${type}-list`);
    if (!tagList) {
        console.warn(`❌ 알 수 없는 type: ${type}`);
        return;
    }
    tagList.innerHTML = '';

    const folded = document.getElementById('tag-toggle-fold')?.classList.contains('folded');
    const shown = folded ? list.slice(0, 5) : list;

    const items = createTagList(type, shown);
    items.forEach(li => tagList.appendChild(li));

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

function renderDetails(detail) {

    const intro = document.getElementById('intro');
    const positive = document.getElementById('positive');
    const negative = document.getElementById('negative');
    const mechanism = document.getElementById('mechanism');
    const dosage = document.getElementById('dosage');
    intro.innerHTML = `${detail.intro}`;
    positive.innerHTML = `${detail.positive}`;
    negative.innerHTML = `${detail.negative}`;
    mechanism.innerHTML = `${detail.mechanism}`;
    dosage.innerHTML = `${detail.dosage}`;
}

function renderButton(boxId, id, text, cls){
    const box = document.getElementById(boxId);
    const button = document.createElement('button');
    if(!box){
        return;
    }
    button.textContent = text;
    button.id=id;
    button.className = cls;
    box.appendChild(button);
}

function renderPages(list,pageMap) {
    const tbody = document.getElementById('page-body');
    tbody.innerHTML = '';
    pageMap.clear();
    const isFolded = document.getElementById('toggle-fold')?.classList.contains('folded');
    const shown = isFolded ? list.slice(0, 5) : list;
    const editMode = document.getElementById('toggle-change')?.classList.contains('execute');

    shown.forEach(item => {
        pageMap.set(item.id, item);
        const row = document.createElement('tr');
        row.dataset.id = item.id;
        row.innerHTML = `
      <td>
      ${editMode
            ? `<input name="title" value="${item.title}" class="w-32"/>`
            : `${item.title}`
       }
      </td>
      <td>
      ${editMode
            ? `<input name="link" value="${item.link}" class="w-32"/>`
            : `${item.link}`
       }
      </td>
        <td>${item.viewCount}</td>
        <td>${item.bookMarkCount}</td>
      <td>
      ${editMode
            ? `<input name="level" value="${item.level}" class="w-16"/>`
            : `${item.level}`
       }
      </td>
        <td>${item.updatedAt}</td>
      ${editMode ? `<td>        
           <button class="save-btn" data-id="${item.id}">저장</button>
         </td>` : ''}
      `;
        tbody.appendChild(row);
    });
}

export {
renderSupplements,
renderJournals,
renderTags,
renderModal,
renderDetails,
renderButton,
renderPages
};