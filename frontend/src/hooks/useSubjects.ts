'use client';

import { useEffect, useState, useCallback } from 'react';
import { api } from '@/lib/api/client';
import type { Subject } from '@/types/api';

export function useSubjects() {
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadSubjects = useCallback(() => {
    api.get<Subject[]>('/subjects')
      .then(setSubjects)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadSubjects();
  }, [loadSubjects]);

  const createSubject = async (name: string, description?: string, color?: string) => {
    const newSubject = await api.post<Subject>('/subjects', { name, description, color });
    setSubjects((prev) => [...prev, newSubject]);
    return newSubject;
  };

  const updateSubject = async (id: string, updates: Partial<Pick<Subject, 'name' | 'description' | 'color'>>) => {
    const updated = await api.patch<Subject>(`/subjects/${id}`, updates);
    setSubjects((prev) => prev.map((s) => (s.id === id ? updated : s)));
    return updated;
  };

  const deleteSubject = async (id: string) => {
    await api.delete(`/subjects/${id}`);
    setSubjects((prev) => prev.filter((s) => s.id !== id));
  };

  return { subjects, loading, error, createSubject, updateSubject, deleteSubject, refresh: loadSubjects };
}
