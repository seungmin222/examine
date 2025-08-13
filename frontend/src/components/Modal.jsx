import React from 'react';
import SearchForm from "@/components/SearchForm.jsx";



export default function Modal({  modalState, setModalState, modals, setModals, types, getTagFn, children }) {
    return (
        <section id="modal" className="modal hidden" role="dialog" aria-modal="true">
            <div className="modal-content">
                {/* 고정 컨트롤러 */}
                <div id="modal-controller" className="flex-end">
                    <div className="flex justify-end gap-2">
                        <SearchForm
                            id="modal-search-form"
                            placeholder="태그 검색"
                            onSubmit={searchTags}
                            inputClass='w-24'
                        />
                        <select name="typeId" id="modal-sort">
                            <option value="korName">한글순</option>
                            <option value="engName">영어순</option>
                        </select>

                        <button id="modal-delete">전체 해제</button>
                        <button id="modal-reset">초기화</button>
                        <button id="modal-close">닫기</button>
                    </div>
                </div>

                {/* 가변 영역(체크박스/테이블 등) */}
                {children}
            </div>
        </section>
    );
}
