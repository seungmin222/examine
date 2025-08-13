import { http, toQuery } from './http';

const BASE = '/api/search'; // 백엔드에 맞게 조정

export async function getTagSuggests({
                                          type = [''],
                                          keyword = '',
                                          signal,
                                      } = {}) {
    const query = toQuery({ type, keyword});
    return http(`${BASE}/tags?${query}`, { signal });
}

export async function getJournalSuggests({
                                         keyword = '',
                                         signal,
                                     } = {}) {
    const query = toQuery({keyword});
    return http(`${BASE}/journals?${query}`, { signal });
}

export async function getTotalSuggests({
                                             keyword = '',
                                             signal,
                                         } = {}) {
    const query = toQuery({keyword});
    return http(`${BASE}?${query}`, { signal });
}