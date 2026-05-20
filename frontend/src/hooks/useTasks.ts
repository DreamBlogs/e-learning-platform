'use client';

import { useEffect, useState, useCallback } from 'react';
import { api } from '@/lib/api/client';
import type { Task } from '@/types/api';

export function useTasks() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);

  const loadTasks = useCallback(() => {
    api.get<Task[]>('/tasks')
      .then(setTasks)
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  const toggleTask = async (taskId: string) => {
    const updated = await api.patch<Task>(`/tasks/${taskId}/toggle`);
    setTasks((prev) => prev.map((t) => (t.id === taskId ? updated : t)));
  };

  const createTask = async (task: Omit<Task, 'id' | 'createdAt' | 'completed'>) => {
    const body = {
      ...task,
      dueDate: task.dueDate ? new Date(task.dueDate).toISOString() : undefined,
    };
    const newTask = await api.post<Task>('/tasks', body);
    setTasks((prev) => [...prev, newTask]);
    return newTask;
  };

  const deleteTask = async (taskId: string) => {
    await api.delete(`/tasks/${taskId}`);
    setTasks((prev) => prev.filter((t) => t.id !== taskId));
  };

  return { tasks, loading, toggleTask, createTask, deleteTask, refresh: loadTasks };
}
