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

function renderSupplements(list) {
    const tbody = document.getElementById('supplement-body');
    tbody.innerHTML = '';
    const isFolded = document.getElementById('toggle-fold')?.classList.contains('folded');
    const shown = isFolded ? list.slice(0, 5) : list;
    const editMode = document.getElementById('toggle-change')?.classList.contains('execute');
    const deleteMode = document.getElementById('toggle-delete')?.classList.contains('execute');

    shown.forEach(item => {
        const row = document.createElement('tr');
        row.dataset.id = item.id;
        const types = item.types?.map(e => e.name).join(', ') || '';
        const effects = item.effects?.map(e => `<span class="${e.tier}">${e.effectName} (${e.tier ?? '-'})</span>`).join(', ') || '';
        const sideEffects = item.sideEffects?.map(e => `<span class="${e.tier}">${e.sideEffectName} (${e.tier ?? '-'})</span>`).join(', ') || '';
        const link = `http://localhost:8080/detail.html?id=${item.id}`;

        row.innerHTML = `
      <td>
      ${editMode
      ? `<input name="korName" value="${item.korName}" class="long"/>`
      : `<a href = ${link}>${item.korName}</a>`
      }
      </td>
      <td>
      ${editMode
      ? `<input name="engName" value="${item.engName}" class="long"/>`
      : `<a href = ${link}>${item.engName}</a>`
      }
      </td>
      <td>${types}</td>
      <td>
      ${editMode
      ? `<input name="dosage" value="${item.dosage}"/>`
      : item.dosage
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
      <div class="edit-group">
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
    const deleteMode = document.getElementById('toggle-delete')?.classList.contains('execute');

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
              <option value="${opt.value}" class="w-32 ${opt.tier}" ${item.trialDesign?.id === opt.value ? 'selected' : ''}>
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
                     <option value="open-label" class='w-32' ${item.blind === 'open-label' ? 'selected' : ''}>open-label</option>
                     <option value="single-blind" class="w-32 B" ${item.blind === 'single-blind' ? 'selected' : ''}>single-blind</option>
                     <option value="double-blind" class="w-32 A" ${item.blind === 'double-blind' ? 'selected' : ''}>double-blind</option>
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
              ? `<input name="duration-value" type="number" value="${item.duration.value}"/>
                 <select name="duration-unit">
                    <option value="day" ${item.duration.unit === 'day' ? 'selected' : ''}>day</option>
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
          createTooltip(`tooltip-${item.id}`, item.summary, 'right');
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



export {
renderSupplements,
renderJournals,
renderTags,
renderModal,
renderDetails
};