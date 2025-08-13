import { useEffect, useRef, cloneElement, isValidElement } from "react";
import { createPortal } from "react-dom";
import { createPopper } from "@popperjs/core";

export default function Tooltip({
                                    trigger,
                                    content,
                                    placement = "top",
                                    tail = false,
                                    bodyClass = "",
                                }) {
    const anchorRef = useRef(null);
    const tooltipRef = useRef(null);
    const arrowRef = useRef(null);
    const popperRef = useRef(null);
    const hideTimer = useRef(null);

    const distance = tail ? 15 : 0;

    // Popper 생성/옵션 갱신 + 이벤트 바인딩
    useEffect(() => {
        const anchor = anchorRef.current;
        const tooltip = tooltipRef.current;
        if (!anchor || !tooltip) return;

        popperRef.current?.destroy();
        popperRef.current = null;

        const modifiers = [
            { name: "offset", options: { offset: [0, distance] } },
            { name: "preventOverflow",
                options: {
                 boundary: "viewport",
                 rootBoundary: 'viewport',
                 tether: true, // 참조 요소에 묶어서 너무 멀어지지 않게
                 altAxis: true
                }
            },
        ];
        if (tail && arrowRef.current) {
            modifiers.push({ name: "arrow", options: { element: arrowRef.current } });
        }

        popperRef.current = createPopper(anchor, tooltip, { placement, modifiers });

        const show = () => {
            clearTimeout(hideTimer.current);
            tooltip.classList.remove("hidden");
            popperRef.current?.update();
        };
        const hide = () => {
            hideTimer.current = setTimeout(() => {
                tooltip.classList.add("hidden");
            }, 100);
        };

        anchor.addEventListener("mouseenter", show);
        anchor.addEventListener("mouseleave", hide);
        tooltip.addEventListener("mouseenter", show);
        tooltip.addEventListener("mouseleave", hide);

        return () => {
            anchor.removeEventListener("mouseenter", show);
            anchor.removeEventListener("mouseleave", hide);
            tooltip.removeEventListener("mouseenter", show);
            tooltip.removeEventListener("mouseleave", hide);
            popperRef.current?.destroy();
            popperRef.current = null;
            clearTimeout(hideTimer.current);
        };
    }, [placement, tail, distance]);

    // 트리거에 ref 연결 (DOM 요소면 clone, 아니면 감싸기)
    const triggerEl =
        isValidElement(trigger) && typeof trigger.type === "string"
            ? cloneElement(trigger, { ref: anchorRef })
            : (
                <span ref={anchorRef} className="inline align-baseline">
          {trigger}
        </span>
            );

    const portalTarget = typeof document !== "undefined" ? document.body : null;

    return (
        <>
            {triggerEl}

            {portalTarget && createPortal(
                <div
                    ref={tooltipRef}
                    className="tooltip-box hidden"
                    role="tooltip"
                    aria-hidden="true"
                >
                    <div className={`tooltip-body ${bodyClass}`}>{content}</div>

                    {tail && (
                        <div ref={arrowRef} className="tooltip-tail-wrapper" data-popper-arrow>
                            <div className="tooltip-tail-border" />
                            <div className="tooltip-tail" />
                        </div>
                    )}
                </div>,
                portalTarget
            )}
        </>
    );
}
