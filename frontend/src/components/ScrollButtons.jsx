// components/ScrollButtons.jsx
import {useEffect} from "react";
import {loadScrollEvent} from "../functions/index.js";

export default function ScrollButtons() {

    useEffect(() => {
        loadScrollEvent();
    }, []);


    return (
        <div id="page-scroll-remote" className="scroll">
            {/* 위로 스크롤 버튼 */}
            <button id="top-scroll" type="button">
                <svg
                    className="scroll-progress"
                    width="40"
                    height="40"
                    viewBox="0 0 100 100"
                >
                    {/* 배경 원 - 회색 테두리 등 비활성 상태 표현 */}
                    <circle
                        className="scroll-circle-bg"
                        cx="50"
                        cy="50"
                        r="45"
                        strokeWidth="10"
                        fill="none"
                    />
                    {/* 채워지는 원 - 스크롤 비율 시각화 */}
                    <circle
                        className="scroll-circle"
                        cx="50"
                        cy="50"
                        r="45"
                        strokeWidth="10"
                        fill="none"
                    />
                    {/* 아이콘 - 위쪽 화살표 경로 */}
                    <path
                        d="M40 30 L60 50 L40 70"
                        strokeWidth="4"
                        fill="none"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                </svg>
            </button>

            {/* 아래로 스크롤 버튼 */}
            <button id="bottom-scroll" type="button">
                <svg
                    className="scroll-progress rotate-270"
                    width="40"
                    height="40"
                    viewBox="0 0 100 100"
                >
                    {/* 배경 원 */}
                    <circle
                        className="scroll-circle-bg"
                        cx="50"
                        cy="50"
                        r="45"
                        strokeWidth="10"
                        fill="none"
                    />
                    {/* 채워지는 원 */}
                    <circle
                        className="scroll-circle"
                        cx="50"
                        cy="50"
                        r="45"
                        strokeWidth="10"
                        fill="none"
                    />
                    {/* 아이콘 - 아래쪽 화살표 경로 */}
                    <path
                        d="M60 30 L40 50 L60 70"
                        strokeWidth="4"
                        fill="none"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                </svg>
            </button>
        </div>
    );
}
