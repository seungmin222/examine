// AlarmContainer.jsx
import { useEffect, useRef, useState } from "react";

// --- 아주 얇은 이벤트버스 (구독자 목록) ---
const subscribers = new Set();

// 외부에서 호출할 API: createAlarm(message, ttlMs?)
export const createAlarm = (message = "새 알림이 도착했습니다.", link='', ttl = 10000) => {
    const id =
        (globalThis.crypto?.randomUUID?.() ?? `${Date.now()}_${Math.random().toString(36).slice(2)}`);
    subscribers.forEach((add) => add({ id, message, link, ttl }));
};

const ms = (n) => n ?? 10000;

export default function AlarmContainer({ className = "" }) {
    const [alarms, setAlarms] = useState([]);
    const timersRef = useRef(new Map()); // id -> timeout
    const mountedRef = useRef(true);

    // 구독 등록/해제
    useEffect(() => {
        mountedRef.current = true;
        const add = (alarm) => {
            if (!mountedRef.current) return;

            // 추가
            setAlarms((prev) => [...prev, { ...alarm, closing: false }]);

            // TTL 지나면 닫힘 애니메이션 -> 500ms 뒤 제거
            const to = setTimeout(() => {
                startClose(alarm.id);
            }, ms(alarm.ttl));
            timersRef.current.set(alarm.id, to);
        };

        subscribers.add(add);
        return () => {
            mountedRef.current = false;
            subscribers.delete(add);
            // 남아있는 타이머 정리
            timersRef.current.forEach(clearTimeout);
            timersRef.current.clear();
        };
    }, []);

    // 닫기(클릭 or TTL)
    const startClose = (id) => {
        // 애니메이션 클래스 부여
        setAlarms((prev) => prev.map((a) => (a.id === id ? { ...a, closing: true } : a)));

        // 기존 TTL 타이머 있으면 정리
        const t = timersRef.current.get(id);
        if (t) {
            clearTimeout(t);
            timersRef.current.delete(id);
        }

        // 500ms 뒤 실제 제거
        setTimeout(() => {
            setAlarms((prev) => prev.filter((a) => a.id !== id));
        }, 500);
    };

    return (
        <div id="alarm-container" className={`alarm-container ${className}`}>
            {alarms.map((a) => (
                <div
                    key={a.id}
                    className={`alarm-toast ${a.closing ? "hide" : ""}`}
                    onClick={() => startClose(a.id)}
                    role="status"
                >
                    <p className='m-0'>
                        {a.message}
                    </p>
                    {a.link && (
                        <a
                            href={a.link}
                            onClick={(e) => e.stopPropagation()} // 부모 div 클릭 닫힘 방지
                        >
                            바로가기
                        </a>
                    )}
                </div>
            ))}
        </div>
    );
}
