const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

async function fetchApi<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options?.headers || {}),
  };

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Request failed' }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }

  const data = await response.json();
  return data.data as T;
}

export const api = {
  get: <T>(endpoint: string) => fetchApi<T>(endpoint),
  post: <T>(endpoint: string, body?: unknown) =>
    fetchApi<T>(endpoint, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(endpoint: string, body?: unknown) =>
    fetchApi<T>(endpoint, { method: 'PUT', body: JSON.stringify(body) }),
  patch: <T>(endpoint: string, body?: unknown) =>
    fetchApi<T>(endpoint, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: (endpoint: string) => fetchApi(endpoint, { method: 'DELETE' }),
};
