'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api/client';
import type { User } from '@/types/api';

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
    if (!token) {
      setLoading(false);
      return;
    }

    api.get<User>('/auth/me')
      .then(setUser)
      .catch(() => {
        localStorage.removeItem('token');
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (email: string, password: string) => {
    const { accessToken, ...userData } = await api.post<
      { accessToken: string } & User
    >('/auth/login', { email, password });
    localStorage.setItem('token', accessToken);
    setUser(userData);
    return userData;
  };

  const register = async (email: string, password: string, displayName: string) => {
    const { accessToken, ...userData } = await api.post<
      { accessToken: string } & User
    >('/auth/register', { email, password, displayName });
    localStorage.setItem('token', accessToken);
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    router.push('/login');
  };

  return { user, loading, login, register, logout };
}
