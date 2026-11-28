import axios from 'axios';

let accessToken: string | null = null;

// The refresh token is HttpOnly and must never be read by JavaScript.
// The optional second argument is retained temporarily so existing login
// callers do not need to change in the same small patch.
export const setTokens = (access: string | null, _refresh?: string | null) => {
  void _refresh;
  accessToken = access;
};

const axiosClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

axiosClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  const xsrf = document.cookie
    .split('; ')
    .find((row) => row.startsWith('XSRF-TOKEN='))
    ?.split('=')[1];
  if (xsrf) {
    config.headers['X-XSRF-TOKEN'] = decodeURIComponent(xsrf);
  }
  return config;
});

axiosClient.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      try {
        // Use the plain Axios client so a failed refresh does not recursively
        // trigger this same response interceptor. The browser sends the
        // HttpOnly refresh_token cookie because withCredentials is enabled.
        const { data } = await axios.post(
          '/api/user/refresh-token',
          undefined,
          { withCredentials: true },
        );
        setTokens(data.accessToken);
        original.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosClient(original);
      } catch (err) {
        setTokens(null);
        window.location.href = '/login';
        return Promise.reject(err);
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;