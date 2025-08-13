export function applyStoredTheme() {
    const theme = localStorage.getItem('selectedTheme');
    if (theme) {
        const html = document.documentElement;
        html.classList.forEach((c) => {
            if (c.startsWith('theme-')) html.classList.remove(c);
        });
        html.classList.add('theme-' + theme);
        console.log("theme " + theme);
    }
}