export async function init() {

    document.getElementById("register-form").addEventListener("submit", async (e) => {
        e.preventDefault();

        const duplicate = document.getElementById("duplicate-button");
        const username = e.target.username.value;
        const password = e.target.password.value;
        const check = e.target.check.value;
        if (duplicate.dataset.check!=="true"||duplicate.dataset.username!==username) {
            alert("아이디 중복 확인을 해 주세요.");
            return;
        }

        if (username.length < 4){
            alert("아이디가 너무 짧습니다.");
            return;
        }

        if (username.length > 12){
            alert("아이디가 너무 깁니다.");
            return;
        }

        if (password.length < 8){
            alert("비밀번호가 너무 짧습니다.");
            return;
        }

        if (password.length > 16){
            alert("비밀번호가 너무 깁니다.");
            return;
        }

        if (password !== check) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        const redirect = new URLSearchParams(window.location.search).get("redirect") || "/";

        const res = await fetch("/api/user/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username,
                password,
                redirect
            })
        });


        if (res.ok) {
            window.location.href = redirect;
        } else {
            alert("회원가입 실패");
        }
    });

    document.getElementById("duplicate-button").addEventListener("click", async (e) => {
        e.preventDefault();
        const username = document.getElementById("username").value;
        const res = await fetch(`/api/user/duplication?username=${username}`);


        if (res.ok) {
            const isDuplicate = await res.text();
            if (isDuplicate === "true") {
                alert("아이디가 중복입니다.");
            } else {
                e.target.dataset.check = true;
                e.target.dataset.username = username;
                alert("사용가능한 아이디입니다.");
            }
        } else {
            alert("조회 실패");
        }
    })
}

