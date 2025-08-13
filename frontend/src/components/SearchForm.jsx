import { ReactComponent as SearchIcon } from '@/assets/search.svg';
import { useRef, useState } from 'react';

export default function SearchForm({
                                       id = 'search-form',
                                       submitFn = () => {},
                                       changeFn = () => Promise.resolve([]),
                                       placeholder = '검색',
                                       formClass = '',
                                       inputClass = 'w-48',
                                       suggestClass ='',
                                       debounceMs = 200,
                                   }) {
    const [value, setValue] = useState('');
    const [suggests, setSuggests] = useState([]);
    const timerRef = useRef(null);
    const abortRef = useRef(null);
    const inputRef = useRef(null);
    const cacheRef = useRef(new Map());      // key: normalized query → value: suggests[]
    const inflightRef = useRef(new Map());   // key → Promise (동일 키 동시요청 dedupe)
    const MAX_CACHE = 200;                   // LRU 용량
    const TTL_MS = 5 * 60 * 1000;           // 캐시 유효기간(선택)

// 헬퍼
    const normKey = (s) => s.trim().toLowerCase();

    const setCache = (key, data) => {
        // LRU: 초과 시 가장 오래된 항목 제거
        if (cacheRef.current.size >= MAX_CACHE) {
            const firstKey = cacheRef.current.keys().next().value;
            cacheRef.current.delete(firstKey);
        }
        cacheRef.current.set(key, { data, ts: Date.now() });
    };

    const getCache = (key) => {
        const hit = cacheRef.current.get(key);
        if (!hit) return null;
        if (Date.now() - hit.ts > TTL_MS) { cacheRef.current.delete(key); return null; }
        return hit.data;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        submitFn(value.trim());
        setSuggests([]);
    };

    const handleChange = (e) => {
        const v = e.target.value;
        setValue(v);

        clearTimeout(timerRef.current);

        const trimmed = v.trim();
        if (!trimmed) {
            setSuggests([]);
            if (abortRef.current) abortRef.current.abort();
            return;
        }

        timerRef.current = setTimeout(async () => {
            // 이전 요청 취소
            if (abortRef.current) abortRef.current.abort();
            const controller = new AbortController();
            abortRef.current = controller;

            const key = normKey(trimmed);

            // 1) 캐시 히트면 즉시 반환
            const cached = getCache(key);
            if (cached) {
                setSuggests(cached);
                return;
            }

            // 2) 동일 키 인플라이트 공유
            if (inflightRef.current.has(key)) {
                try {
                    const list = await inflightRef.current.get(key);
                    setSuggests(Array.isArray(list) ? list : []);
                } catch (_) {}
                return;
            }

            // 3) 실제 요청
            const p = (async () => {
                const list = await changeFn(key, { signal: controller.signal });
                const arr = Array.isArray(list) ? list : [];
                setCache(key, arr);
                return arr;
            })();

            inflightRef.current.set(key, p);
            try {
                const data = await p;
                setSuggests(data);
            } catch (_) {
                // AbortError 등은 무시
            } finally {
                inflightRef.current.delete(key);
            }
        }, debounceMs);
    };

    const pick = (s) => {
        const name = s?.name ?? s?.label ?? '';
        setValue(name);
        setSuggests([]);
        // 다시 포커스 주고 싶으면
        inputRef.current?.focus();
    };

    return (
        // ✅ 래퍼를 relative로: 드롭다운은 여기를 기준으로 top-full
        <div className={`${formClass} relative inline-block z-50`}>
            <form id={id} className="flex-row" onSubmit={handleSubmit}>
                <input
                    ref={inputRef}
                    type="search"
                    name="input"
                    value={value}
                    className={`${inputClass} rounded-r-none`}
                    placeholder={placeholder}
                    onChange={handleChange}
                    autoComplete="off"
                />
                <button
                    className="rounded-l-none flex justify-center items-center w-8"
                    type="submit"
                >
                    <SearchIcon className="w-4 h-4 fill-white" />
                </button>
            </form>

            {/* ✅ 폼 “아래”에 붙도록 absolute top-full (폼의 레이아웃엔 영향 없음) */}
            {suggests.length > 0 && (
                <div
                    className="absolute left-0 top-full z-20 mt-1 w-full rounded-xl border bg-white shadow
               max-h-64 overflow-auto"
                    onMouseDown={(e) => e.preventDefault()}
                >
                    {suggests.map((s, i) => (
                        <div
                            key={`${s?.type ?? 'x'}-${s?.id ?? i}`}
                            className={`${suggestClass} px-3 py-2 cursor-pointer hover:bg-gray-100`}
                            onClick={() => pick(s)}
                            title={(s?.name ?? s?.label ?? '').toString()}
                        >
                            {/* ✅ 한 줄 고정 + … (폭은 부모 w-full로 제한) */}
                            <div className="block w-full overflow-hidden text-ellipsis !whitespace-nowrap break-normal">
                                {s?.name ?? s?.label ?? ''}
                            </div>

                            {/* 아래는 그대로 세로 배치 */}
                            {s?.type && (
                                <div className="text-xs text-gray-500">{s.type}</div>
                            )}
                        </div>
                    ))}
                </div>
            )}

        </div>
    );
}
