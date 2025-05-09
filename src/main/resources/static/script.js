// 초기 로딩
document.addEventListener('DOMContentLoaded', async () => {

 fetch('/nav.html')
    .then(res => res.text())
    .then(html => {
      document.getElementById('navbar').innerHTML = html;
    });

});
