'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api/client';
import type { Task } from '@/types/api';

export function useTasks() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<Task[]>('/tasks')
      .then(setTasks)
      .finally(() => setLoading(false));
  }, []);

  const toggleTask = async (taskId: string) => {
    const updated = await api.patch<Task>(`/tasks/${taskId}/toggle`);
    setTasks((prev) => prev.map((t) => (t.id === taskId ? updated : t)));
  };

  return { tasks, loading, toggleTask };
}
