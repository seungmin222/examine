import {
    loadBasicModule,
    loadBasicEvent
} from '/util/load.js';
import {
    sidebarToggle
} from '/util/event.js';

document.addEventListener('DOMContentLoaded', async () => {
    await loadBasicEvent();
    sidebarToggle('sidebar','sidebar-toggle');
    const path = window.location.pathname.replace(/^\/+|\/+$/g, ''); // trim slashes
    const segments = path.split('/'); // ex: ['table', 'journal']

    let base = segments.join('/');
    if (base === '') base = 'home';

    const htmlPath = `/module/${base}.html`;
    const jsPath = `/module/${base}.js`;

    const name = path.split('/').pop() || 'home';
    document.title = `${name}` ?? 'Examine';
    try {
        const html = await fetch(htmlPath).then(res => res.text());
        document.getElementById('content').innerHTML = html;

        const module = await import(jsPath);
        console.log("📁 경로 확인:", htmlPath, jsPath);
        if (module?.init) {
            module.init();//컨트롤러, 버튼 등은 모듈 로딩 후에 로딩
            await loadBasicModule();
            console.log("✅ init 함수 실행 시작");
        }
        else{
            console.log("📛 init 함수 실행 실패");
        }

    } catch (e) {
        console.error(`📛 모듈 로딩 실패: ${base}`, e);
        document.getElementById('content').innerHTML = `<p>해당 페이지를 불러올 수 없습니다.</p>`;
    }
});

