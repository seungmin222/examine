// <head> 에서 로딩
const theme = localStorage.getItem('selectedTheme');
if (theme) {
  const html = document.documentElement;

  html.classList.forEach(c => {
    if (c.startsWith('theme-')) document.documentElement.classList.remove(c);
  });
  html.className = 'theme-' + theme;
}
