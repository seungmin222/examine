import {createPath, loadBasicEvent, loadBasicModule, sidebarToggle} from '/util/index.js';


document.addEventListener('DOMContentLoaded', async () => {
    await loadBasicEvent();
    sidebarToggle('sidebar','sidebar-toggle');
    const base = createPath();

    const htmlPath = `/module/${base}.html`;
    const jsPath = `/module/${base}.js`;

    const name = base.split('/').pop() || 'home';
    document.title = `${name}` ?? 'Examine';
    const content = document.getElementById('content');

    try {
        content.innerHTML = await fetch(htmlPath).then(res => res.text());
        await MathJax.typesetPromise([content]);
        await loadBasicModule();

        try {
            const module = await import(jsPath);
            if (module?.init) {
                module.init();
                console.log("✅ init 함수 실행 시작");
            } else {
                console.log("📛 init 함수 없음");
            }
        } catch (e) {
            console.warn(`⚠️ JS 모듈 없음 또는 로딩 실패 (${jsPath})`, e);
        }
        console.log("📁 경로 확인:", htmlPath, jsPath);
    } catch (e) {
        console.error(`📛 모듈 로딩 실패: ${base}`, e);
        content.innerHTML = `<p>해당 페이지를 불러올 수 없습니다.</p>`;
    }
});

