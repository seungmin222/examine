import { http, toQuery } from './http';

const BASE = '/api/tags'; // 백엔드에 맞게 조정

export async function getTags({
                                      type = [''],
                                      offset = 0,
                                      limit = 30,
                                      sort = 'korName',
                                      asc = true,
                                      keyword = '',
                                      signal,
                                  } = {}) {
    const query = toQuery({ type, offset, limit, sort, asc, keyword});
    return http(`${BASE}?${query}`, { signal });
}

export async function updateTag(id, payload, { signal } = {}) {
    if (!id) throw new Error('update: id가 필요합니다');
    return http(`${BASE}/${id}`, { method: 'PUT', body: payload, signal });
}

export async function deleteTag(id, { signal } = {}) {
    if (!id) throw new Error('delete: id가 필요합니다');
    return http(`${BASE}/${id}`, { method: 'DELETE', signal });
}