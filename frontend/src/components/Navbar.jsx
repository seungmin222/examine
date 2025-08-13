// components/Navbar.jsx
import {useEffect, useState} from "react";
import {
    createNewAlarm,
    createPathWithParam,
    deleteCookie,
    getCookie, onThemeClick,
    updateCart
} from "../functions/index.js";
import {
    createAlarm
} from "@/components/AlarmContainer.jsx";
import {ReactComponent as PillIcon} from '@/assets/pill.svg';
import {ReactComponent as NoticeIcon} from '@/assets/notice.svg';
import {ReactComponent as CopyIcon} from '@/assets/copy.svg';
import Tooltip from "@/components/Tooltip.jsx";
import SearchForm from "@/components/SearchForm.jsx";
import {getTotalSuggests} from "@/functions/api/search.js";


export default function Navbar() {

    const redirect = typeof window !== 'undefined'
        ? encodeURIComponent(window.location.pathname + window.location.search + window.location.hash)
        : '';
    const [user, setUser] = useState(null);
    const [page, setPage] = useState(null);
    const [iherbCoupons, setIherbCoupons] = useState([]);

    useEffect(() => {
        const init = async () => {
            await loadUser();
            await loadPage();
            await loadIherbCoupon();
            receiveAlarm(getAlarm);
        };
        init();
    }, []);

    const loadUser = async () => {
        const res = await fetch("/api/user/me", { credentials: "include" });
        if (res.ok) {
            const data = await res.json();
            setUser(data);
        } else if (res.status === 401 && getCookie("refresh")) {
            const refreshRes = await fetch("/api/redis/refresh", {
                method: "POST",
                credentials: "include",
            });
            if (refreshRes.ok) return loadUser();
            deleteCookie("refresh");
            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
        }
    };

    const loadPage = async () => {
        const base = createPathWithParam();
        const res = await fetch(`/api/pages/current?link=/${encodeURIComponent(base)}`);
        if (res.ok) {
            const data = await res.text();
            setPage(data);
        } else {
            console.error("페이지 ID를 불러오지 못했습니다");
        }
    };

    const loadIherbCoupon = async () => {
        try {
            const res = await fetch("/api/sale/iherb");
            const data = await res.json();
            setIherbCoupons(res.ok && Array.isArray(data) ? data : []);
        } catch (e) {
            console.error("iHerb 쿠폰 로딩 실패", e);
            setIherbCoupons([]);
        }
    };

    const receiveAlarm = () => {
        const eventSource = new EventSource('/api/redis/alarm/subscribe');

        eventSource.addEventListener('alarm', async (e) => {
            try {
                const dto = JSON.parse(e.data); // 서버에서 보낸 AlarmResponse JSON

                const normalized = {
                    alarm: {
                        id: dto.id,
                        message: dto.message,
                        link: dto.link,
                        supplementName: dto.supplementName,
                        time: dto.time,
                    },
                };

                setUser(prev => {
                    if (!prev) return prev;
                    return { ...prev, alarms: [normalized, ...(prev.alarms ?? [])] };
                });

                createAlarm(dto.message ?? '새 알림이 도착했습니다.', dto.link);
            } catch (err) {
                console.error('알림 JSON 파싱 실패:', err, e.data);
            }
        });

        eventSource.onerror = (err) => {
            console.error('🔌 SSE 연결 오류', err);
            eventSource.close();
        };
    };

    const getAlarm = async () => {
        const res = await fetch("/api/user/alarm", { method: "GET", credentials: "include" });

        if (res.ok) {
            const data = await res.json();
            setUser((prev) => {
                if (!prev) return prev;
                return {
                    ...prev,
                    alarms: data.list,
                    unreadCount: prev.unreadCount+1,
                };
            });
        } else {
            console.error("알람을 불러오지 못했습니다");
        }

    };

    const readAllAlarm = async () => {
        await fetch("/api/user/alarm/readAll", { method: "PUT", credentials: "include" });
        setUser((prev) => {
            if (!prev) return prev;
            return {
                ...prev,
                alarms: prev.alarms.map((a) => ({ ...a, isRead: true })),
                unreadCount: 0,
            };
        });
    };

    const deleteAllAlarm = async () => {
        await fetch("/api/user/alarm/deleteAll", { method: "DELETE", credentials: "include" });
        setUser((prev) => {
            if (!prev) return prev;
            return {
                ...prev,
                alarms: [],
                unReadCount: 0,
            };
        });
    };

    const deleteAllBookmarks = async () => {
        await fetch("/api/user/bookmark", { method: "DELETE", credentials: "include" });
        setUser((prev) => {
            if (!prev) return prev;
            return {
                ...prev,
                pages: [],
            };
        });
    };

    const deleteAllCart = async () => {
        await fetch("/api/user/cart", { method: "DELETE", credentials: "include" });
        setUser((prev) => {
            if (!prev) return prev;
            return {
                ...prev,
                products: [],
                totalPrice: 0,
            };
        });
    };

    const logout = async () => {
        await fetch("/api/user/logout", { method: "POST", credentials: "include" });
        deleteCookie("refresh");
        setUser(null);
        alert("로그아웃 되었습니다.");
    };

    const saveBookmark = async () => {
        await fetch(`/api/user/bookmark/${page?.id}`, { method: "POST", credentials: "include" });
        alert("북마크가 저장되었습니다.");
    };

    const deleteAlarm = async (alarmId) => {
        await fetch(`/api/user/alarm/${alarmId}`, { method: "DELETE", credentials: "include" });
        setUser((prev) => {
            if (!prev) return prev;
            const updatedAlarms = prev.alarms.filter((a) => a.alarm.id !== alarmId);
            const newUnreadCount = updatedAlarms.filter((a) => !a.isRead).length;
            return {
                ...prev,
                alarms: updatedAlarms,
                unreadCount: newUnreadCount,
            };
        });
    };

    const deleteCart = async (cartId) => {
        await fetch(`/api/user/cart/${cartId}`, { method: "DELETE", credentials: "include" });
        setUser((prev) => {
            if (!prev) return prev;
            const updatedProducts = prev.products.filter((a) => a.id !== productId);
            return {
                ...prev,
                products: updatedProducts,
            };
        });
    }

    const updateCart = async (productId, quantity, isChecked) => {
        const pid = Number(productId);
        const qty = Number(quantity);

        const updated = { id: pid, quantity: qty, isChecked };
        const res = await fetch(`/api/user/cart`, {
            method: "PUT",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updated),
        });

        setUser((prev) => {
            if (!prev) return prev;
            const products = qty > 0
                ? prev.products.map(p => Number(p.id) === pid ? { ...p, quantity: qty, isChecked } : p)
                : prev.products.filter(p => Number(p.id) !== pid);
            return { ...prev, products };
        });

        const data = await res.json();
        setUser((prev)=>{
           return {...prev, totalPrice : data.data};
        });
    };


    // 복사 헬퍼: 성공/실패 모두 createAlarm 사용
    const copyPromo = async (text) => {
        try {
            await navigator.clipboard.writeText(text);
            createAlarm('코드를 복사했습니다.');
        } catch {
            try {
                // fallback (HTTPS 아니거나 권한 거부 시)
                const ta = document.createElement('textarea');
                ta.value = text;
                ta.setAttribute('readonly', '');
                ta.style.position = 'fixed';
                ta.style.opacity = '0';
                document.body.appendChild(ta);
                ta.select();
                document.execCommand('copy');
                document.body.removeChild(ta);
                createAlarm('코드를 복사했습니다.');
            } catch {
                createAlarm('복사에 실패했습니다. 직접 복사해 주세요.');
            }
        }
    };

    const totalSuggestion = (keyword) => {
        return getTotalSuggests({keyword});
    }


    return (
        <nav className="navbar">
            <div className="flex gap-8">
                <div className="relative">
                    <a href="/" className="hover:no-underline">
                        <button className="dropdown">
                            <PillIcon className="size-5 mr-1" />
                            Examine
                        </button>
                    </a>
                </div>

                <Tooltip
                    trigger = {<button id="table-dropdown-toggle" className="dropdown">목록 관리</button>}
                    content = {
                        <>
                            <a href="/supplement" className="logo">성분 목록</a>
                            <a href="/journal" className="logo">논문 목록</a>
                            <a href="/effect?type=effect" className="logo">기대 효과</a>
                            <a href="/ffect?type=sideEffect" className="logo">부작용</a>
                            <a href="/effect?type=type" className="logo">분류</a>
                            <a href="/brand" className="logo">브랜드 목록</a>
                        </>
                    }
                    placement = 'bottom'
                    bodyClass = 'grid [grid-template-columns:repeat(2,auto)] gap-2'
                />

                <Tooltip
                    trigger = {<button id="guide-dropdown-toggle" className="dropdown">용어 설명</button>}
                    content = {
                    <>
                        <a href="/guide/ebm" className="logo">근거 수준</a>
                        <a href="/guide/shop" className="logo">쇼핑몰</a>
                        <a href="/guide/diet" className="logo">식단 가이드</a>
                        <a href="/guide/api" className="logo">api 사용법</a>
                    </>
                    }
                    placement = 'bottom'
                    bodyClass = 'grid [grid-template-columns:repeat(2,auto)] gap-2'
                />
            </div>

            <div className="flex gap-8 justify-center items-center">
                <SearchForm
                    id='all-search-form'
                    placeholder='통합검색'
                    inputClass='w-64'
                    suggestClass='flex'
                    changeFn={totalSuggestion}
                />

                <div id="button-box" className="flex gap-8 h-fit"></div>

                <Tooltip
                    trigger = { <button id="setting-dropdown-toggle" className="dropdown">기타</button>}
                    content = {
                    <>
                        <Tooltip
                            trigger={<button id="iherb-dropdown-toggle" className="dropdown big">쿠폰</button>}
                            content={
                                <ul id="iherb-coupon" className="flex flex-col gap-2">
                                    {iherbCoupons?.length > 0 ? (
                                        iherbCoupons.map((coupon) => (
                                            <li
                                                key={coupon.link}
                                                className="w-full h-fit cursor-default"
                                            >
                                                <p>📢 {coupon.detail}</p>
                                                <div>
                                                    {coupon.promoCode ? (
                                                        <div className="flex gap-2">
                                                            💸 코드: {coupon.promoCode}{" "}
                                                            <CopyIcon
                                                                className="cursor-pointer"
                                                                onClick={() => copyPromo(coupon.promoCode)}
                                                                />
                                                        </div>
                                                    ) : (
                                                        "❕ 할인코드 없음"
                                                    )}
                                                </div>
                                                <div>
                                                    ⏳ 종료: {coupon.expiresAt ? new Date(coupon.expiresAt).toLocaleString("ko-KR") : "-"}
                                                    <a
                                                        href={coupon.link}
                                                        target="_blank"
                                                        rel="noopener noreferrer"
                                                        className="mr-1"
                                                    >
                                                        👉 쇼핑하러 가기
                                                    </a>
                                                </div>
                                            </li>
                                        ))
                                    ) : (
                                        <li key="no-coupon" className="w-fit text-sm text-gray-500">현재 진행 중인 할인 정보가 없습니다.</li>
                                    )}
                                </ul>
                            }
                            placement="left"
                        />
                        <Tooltip
                            trigger = { <button id="theme-dropdown-toggle" className="dropdown big">테마</button>}
                            content = {
                            <ul id="theme" className="space-y-1" onClick={onThemeClick}>
                                <li data-theme="pink" className="cursor-pointer text-pink-600 hover:bg-pink-100">분홍</li>
                                <li data-theme="blue" className="cursor-pointer text-blue-600 hover:bg-blue-100">하늘</li>
                                <li data-theme="gray" className="cursor-pointer text-gray-600 hover:bg-gray-100">흑백</li>
                                <li data-theme="lavender" className="cursor-pointer text-purple-500 hover:bg-purple-100">보라</li>
                                <li data-theme="green" className="cursor-pointer text-green-600 hover:bg-green-100">초록</li>
                            </ul>
                            }
                            placement = 'left'
                        />
                    </>
                    }
                    placement = 'bottom'
                    bodyClass= 'overflow-visible p-0'
                />
                <Tooltip
                    trigger={
                        <a
                            id="user"
                            href={user ? undefined : `/login?redirect=${redirect}`}
                            className="hover:no-underline"
                        >
                            <button id="user-dropdown-toggle" className="dropdown relative">
                                {user ? "마이페이지" : "로그인"}
                                {user?.unreadCount > 0 && (
                                <NoticeIcon className='absolute -top-1 -right-2 size-4' />
                                )}
                            </button>
                        </a>
                    }
                    content={
                        <div className="relative flex flex-col gap-1">
                            <Tooltip
                                trigger={<button id="user-info-dropdown-toggle" className="dropdown big">내 정보</button>}
                                content={<span id="user-info">{user?.username}</span>}
                                placement="left"
                            />

                            <Tooltip
                                trigger={
                                    <button id="alarm-dropdown-toggle" className="dropdown big">
                                        알림
                                        {user?.unreadCount > 0 && (
                                            <NoticeIcon className='absolute left-16 size-4' />
                                        )}
                                        <div id="alarm-svg" className="relative" />
                                    </button>
                                }
                                content={
                                    <div className="flex flex-col gap-2">
                                        <div className="flex gap-2">
                                            <button className="w-16" onClick={deleteAllAlarm}>전부 삭제</button>
                                            <button className="w-16" onClick={readAllAlarm}>전부 읽음</button>
                                        </div>
                                        <ul id="alarm" className="flex flex-col gap-2">
                                            {user?.alarms?.map(({ alarm, isRead }) => (
                                                <li
                                                    key={alarm.id}
                                                    data-id={alarm.id}
                                                    className={`flex flex-col w-full h-fit items-start p-2 ${isRead ? 'bg-gray-100' : ''}`}
                                                >
                                                    <span className="whitespace-nowrap text-gray-800">{alarm.message}</span>
                                                    <div className="flex w-full h-fit">
                                                        <span className="w-24 text-left">{alarm.time}</span>
                                                        <span
                                                            className="text-red-500 cursor-pointer text-left w-16"
                                                            onClick={() => deleteAlarm(alarm.id)}
                                                        >삭제
                                                        </span>
                                                        <span className="w-16 text-left">{isRead ? '읽음' : ''}</span>
                                                        {alarm.link && (
                                                            <a
                                                                href={alarm.link}
                                                                className="w-20 text-blue-600 underline text-right"
                                                                target="_blank"
                                                                rel="noopener noreferrer"
                                                            >
                                                                바로가기
                                                            </a>
                                                        )}
                                                    </div>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                }
                                placement="left"
                            />

                            <Tooltip
                                trigger={<button id="bookmark-dropdown-toggle" className="dropdown big">북마크</button>}
                                content={
                                    <div className="flex flex-col gap-2" id="bookmark-content">
                                        <div className="flex gap-2">
                                            <button className="w-16" onClick={deleteAllBookmarks}>삭제</button>
                                            <button className="w-16" onClick={saveBookmark}>저장</button>
                                        </div>
                                        <div id="bookmark">
                                            {user?.pages?.map((p) => (
                                                <a key={p.id} href={p.link} className="logo" data-id={p.id}>{p.title}</a>
                                            ))}
                                        </div>
                                    </div>
                                }
                                placement="left"
                            />

                            <Tooltip
                                trigger={<button id="cart-dropdown-toggle" className="dropdown big">장바구니</button>}
                                content={
                                    <div className="flex flex-col gap-2" id="cart-content">
                                        <div className="flex gap-2">
                                            <button className="w-16" onClick={deleteAllCart}>전부 삭제</button>
                                            <p id="total-price">{Number(user?.totalPrice || 0).toLocaleString()}원</p>
                                        </div>
                                        <div id="cart" className="flex flex-col gap-2">
                                            {user?.products?.map(({product, quantity, isChecked }) => (
                                                <li
                                                    key={product.id}
                                                    className=" flex gap-4 items-center justify-start w-full h-fit cursor-default"
                                                >
                                                    <input
                                                        type="checkbox"
                                                        defaultChecked={isChecked}
                                                        onChange={(e) => updateCart(product.id, quantity, !isChecked)}
                                                    />
                                                    <div className="flex flex-col w-full items-start">
                                                        <a href={product.link} className="text-sm font-medium">
                                                            {product.brand.korName} {product.name}
                                                        </a>
                                                        <div className="flex justify-between items-center w-full">
                                                            <input
                                                                type="number"
                                                                defaultValue={quantity}
                                                                min="0"
                                                                className="w-16 border text-center"
                                                                onChange={(e) => updateCart(product.id, e.target.value, isChecked)}
                                                            />
                                                            <div className="flex flex-col justify-start text-sm">
                                                                <span>₩{product.price?.toLocaleString() ?? "?"}</span>
                                                                <span>하루당 ₩{product.pricePerDose?.toLocaleString() ?? "?"}</span>
                                                            </div>
                                                            <span
                                                                className="text-red-500 cursor-pointer"
                                                                onClick={() => deleteCart(product.id)}
                                                            >
                                                                삭제
                                                            </span>
                                                        </div>
                                                    </div>
                                                </li>
                                            ))}
                                        </div>
                                    </div>
                                }
                                placement="left"
                            />

                            <button id="logout" className="dropdown big" onClick={logout}>로그아웃</button>
                        </div>
                    }
                    placement="bottom"
                    bodyClass="overflow-visible p-0"
                />

            </div>
        </nav>
    );
}
