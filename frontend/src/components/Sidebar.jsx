import { ReactComponent as ArrowIcon } from '@/assets/arrow.svg';
import { ReactComponent as SearchIcon } from '@/assets/search.svg';
import { ReactComponent as PillIcon } from '@/assets/pill.svg';
import { ReactComponent as PlusIcon } from '@/assets/plus.svg';
import { ReactComponent as AddIcon } from '@/assets/add.svg';

import { useEffect, useState } from 'react';

const svgMap = {
    search: SearchIcon,
    arrow: ArrowIcon,
    pill: PillIcon,
    plus: PlusIcon,
    add: AddIcon,
    // 여기에 필요한 SVG 이름과 컴포넌트 추가
};

export default function Sidebar() {
    const [closed, setClosed] = useState(false);
    const [headings, setHeadings] = useState([]);

    const handleToggle = () => {
        setClosed(prev => !prev);
    };

    useEffect(() => {
        const content = document.getElementById('content');
        if (!content) {
            console.error('목차를 찾을 수 없습니다.');
            return;
        }

        let timer = null;
        const observer = new MutationObserver(() => {
            if (timer) clearTimeout(timer);
            timer = setTimeout(() => {
                const collected = collectHeadings();
                setHeadings(collected);
            }, 100);
        });

        observer.observe(content, {
            childList: true,
            subtree: true,
        });

        return () => {
            observer.disconnect();
            if (timer) clearTimeout(timer);
        };
    }, []);

    function collectHeadings() {
        const result = [];
        let i = 1;

        while (true) {
            const heading = document.getElementById(`index-${i}`);
            if (!heading) break;

            result.push({
                id: heading.id,
                text: heading.textContent,
                svg: heading.dataset.svg,
            });

            i++;
        }

        return result;
    }

    return (
        <aside id="sidebar" className={`${closed ? 'closed' : ''}`}>
            <ArrowIcon
                id="sidebar-toggle"
                className={`arrow transition-transform ${closed ? '' : 'rotate-180'}`}
                onClick={handleToggle}
            />
            <div id="index" className="text-left space-y-2 mt-4">
                {headings.map((item, idx) => {
                    const Svg = item.svg ? svgMap[item.svg] : null;
                    return (
                        <a
                            key={item.id}
                            href={`#${item.id}`}
                            className="index py-1 flex items-center gap-2"
                        >
                            {Svg && <Svg className="size-5" />}
                            <span>{item.text}</span>
                        </a>
                    );
                })}
            </div>
        </aside>
    );
}
