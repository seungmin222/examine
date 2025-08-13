export async function http(url, { method = 'GET', headers = {}, body, signal } = {}) {
    const init = { method, headers: { 'Content-Type': 'application/json', ...headers }, signal };
    if (body !== undefined) init.body = typeof body === 'string' ? body : JSON.stringify(body);

    const res = await fetch(url, init);
    if (!res.ok) {
        const text = await res.text().catch(() => '');
        throw new Error(`HTTP ${res.status} ${res.statusText}: ${text}`);
    }
    const contentType = res.headers.get('content-type') || '';
    return contentType.includes('application/json') ? res.json() : res.text();
}

export function toQuery(obj = {}) {
    const params = new URLSearchParams();
    const append = (k, v) => params.append(k, v ?? '');

    for (const [k, v] of Object.entries(obj)) {
        if (v === undefined || v === null || v === '') continue;
        if (Array.isArray(v)) {
            if (v.length === 0) continue;
            v.forEach((it) => append(k, String(it)));
        } else if (typeof v === 'object') {
            // 중첩 객체 평탄화 (ex: filters)
            for (const [ik, iv] of Object.entries(v)) {
                if (iv === undefined || iv === null || iv === '') continue;
                if (Array.isArray(iv)) iv.forEach((it) => append(ik, String(it)));
                else append(ik, String(iv));
            }
        } else {
            append(k, String(v));
        }
    }
    return params.toString();
}