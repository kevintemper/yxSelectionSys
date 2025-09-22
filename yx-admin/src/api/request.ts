// yx-admin/src/api/request.ts
import axios, { AxiosError, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// --------- token 工具 ----------
const AT = 'accessToken';
const RT = 'refreshToken';
const getAT = () => localStorage.getItem(AT) || '';
const getRT = () => localStorage.getItem(RT) || '';
const setAT = (t: string) => localStorage.setItem(AT, t);
const setRT = (t: string) => localStorage.setItem(RT, t);
const clearTokens = () => { localStorage.removeItem(AT); localStorage.removeItem(RT); };

// --------- 环境头 X-Env ----------
const getCookie = (k: string) =>
  document.cookie.split('; ').find(x => x.startsWith(`${k}=`))?.split('=')[1];

export type Env = 'gray' | 'prd';

const ENV_KEY = 'env';

const normalizeEnv = (value: string | null | undefined): Env | '' => {
  if (!value) return '';
  const lower = value.toLowerCase();
  return lower === 'gray' ? 'gray' : lower === 'prd' ? 'prd' : '';
};

export const getEnv = (): Env => {
  // 优先 cookie: env=gray|prd，其次 localStorage.env，默认为 prd
  const fromCookie = normalizeEnv(getCookie(ENV_KEY));
  if (fromCookie) return fromCookie;
  const fromLS = normalizeEnv(localStorage.getItem(ENV_KEY));
  return fromLS || 'prd';
};

export const setEnv = (env: Env) => {
  const target: Env = env === 'gray' ? 'gray' : 'prd';
  localStorage.setItem(ENV_KEY, target);
  const maxAge = 7 * 24 * 60 * 60; // 和“记住登录”一致，7 天
  document.cookie = `${ENV_KEY}=${target}; path=/; max-age=${maxAge}`;
  window.dispatchEvent(new CustomEvent('yx-env-change', { detail: target }));
};

// --------- 刷新队列，避免并发重复刷新 ----------
let refreshing = false;
let waiters: Array<(t: string) => void> = [];

const doRefresh = async () => {
  refreshing = true;
  try {
    const rt = getRT();
    if (!rt) throw new Error('No refresh token');
    const { data } = await api.post('/auth/refresh', { refreshToken: rt });
    setAT(data.accessToken);
    if (data.refreshToken) setRT(data.refreshToken);
    waiters.forEach(fn => fn(data.accessToken));
    waiters = [];
    return data.accessToken as string;
  } finally {
    refreshing = false;
  }
};

// --------- 请求拦截：带上 Token 与 X-Env ----------
api.interceptors.request.use((cfg: InternalAxiosRequestConfig) => {
  const at = getAT();
  if (at) cfg.headers.Authorization = `Bearer ${at}`;
  const env = getEnv();
  cfg.headers['X-Env'] = env;
  cfg.headers['X-Gray'] = env;
  cfg.headers['SERVICE-TAG'] = env;
  return cfg;
});

// --------- 响应拦截：401 自动刷新并重放；即将过期静默刷新 ----------
api.interceptors.response.use(
  (resp) => {
    if (resp.headers['x-token-expiring'] === 'true') {
      // 静默刷新，不阻塞当前响应
      doRefresh().catch(() => {/* 忽略 */});
    }
    return resp;
  },
  async (error: AxiosError) => {
    const original = error.config as (AxiosRequestConfig & { _retry?: boolean }) | undefined;
    const status = error.response?.status;

    // 仅对 401 且未重试过的请求做自动刷新
    if (status === 401 && original && !original._retry
        && !original.url?.includes('/auth/login')
        && !original.url?.includes('/auth/refresh')) {
      original._retry = true;

      // 已在刷新则排队等待
      if (refreshing) {
        return new Promise(resolve => {
          waiters.push((token: string) => {
            original.headers = { ...(original.headers || {}), Authorization: `Bearer ${token}` };
            resolve(api(original));
          });
        });
      }

      try {
        const token = await doRefresh();
        original.headers = { ...(original.headers || {}), Authorization: `Bearer ${token}` };
        return api(original);
      } catch {
        clearTokens();
        // 跳登录
        window.location.href = '/login';
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
