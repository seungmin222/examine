// Login.jsx
import { useState } from "react";

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading]   = useState(false);
    const [error, setError]       = useState("");

    const onSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        // URL ?redirect=/path 처리 (기본값 "/")
        const redirect =
            new URLSearchParams(window.location.search).get("redirect") || "/";

        try {
            const res = await fetch("/api/login", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ username, password, redirect }),
                credentials: "include",
            });

            if (res.ok) {
                window.location.href = redirect;
            } else {
                // 서버가 에러 메시지를 주면 최대한 보여주기
                let msg = "로그인 실패";
                alert(msg);
                try {
                    const ct = res.headers.get("content-type") || "";
                    if (ct.includes("application/json")) {
                        const data = await res.json();
                        msg = data?.message || msg;
                    } else {
                        const text = await res.text();
                        if (text) msg = text;
                    }
                } catch {}
                setError(msg);
            }
        } catch {
            setError("네트워크 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex justify-center">
            <form id="login-form" className="w-64 flex gap-4 justify-center items-center" onSubmit={onSubmit}>
                <h2 className="text-center">로그인</h2>

                <input
                    className="w-64"
                    type="text"
                    name="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="아이디"
                    autoComplete="username"
                    required
                />

                <input
                    className="w-64"
                    type="password"
                    name="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="비밀번호"
                    autoComplete="current-password"
                    required
                />

                {error && (
                    <p role="alert" className="text-sm text-red-600">
                        {error}
                    </p>
                )}

                <button
                    type="submit"
                    disabled={loading}
                    className="disabled:opacity-50"
                >
                    {loading ? "로그인 중..." : "로그인"}
                </button>

                <p className="text-sm text-center">
                    아직 회원이 아니신가요? <a href="/user/register" className="underline">회원가입</a>
                </p>
            </form>
        </div>
    );
}
