// src/store/perm.ts
import api from '../api/request';

/** 只有本地有 token 才会请求；否则直接返回 null（不发请求） */
export async function loadProfile(quiet = true) {
  const t = localStorage.getItem('accessToken');
  if (!t) return null;

  try {
    const { data } = await api.get('/user/profile');
    // 这里你可以顺便把用户信息丢到全局 store
    return data;
  } catch (e: any) {
    // 开发期可静默吞掉 401，避免把人踢回登录
    if (!quiet) throw e;
    return null;
  }
}
