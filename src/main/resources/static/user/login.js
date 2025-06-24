import {
    loadBasic
} from '/util/load.js';

document.addEventListener('DOMContentLoaded', async () => {

    await loadBasic();

    document.getElementById("login-form").addEventListener("submit", async (e) => {
        e.preventDefault();

        const username = e.target.username.value;
        const password = e.target.password.value;
        const redirect = new URLSearchParams(window.location.search).get("redirect") || "/";

        const res = await fetch("/api/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                username,
                password,
                redirect
            })
        });

        if (res.ok) {
            window.location.href = redirect;
        } else {
            alert("로그인 실패");
        }
    });
});

