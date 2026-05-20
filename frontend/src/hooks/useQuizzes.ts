'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api/client';
import type { Quiz, QuizAttempt } from '@/types/api';

export function useQuizzes(subjectId: string) {
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [attempts, setAttempts] = useState<Record<string, QuizAttempt[]>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!subjectId) return;
    api.get<Quiz[]>(`/subjects/${subjectId}/quizzes`)
      .then(setQuizzes)
      .finally(() => setLoading(false));
  }, [subjectId]);

  const loadAttempts = async (quizId: string) => {
    const quizAttempts = await api.get<QuizAttempt[]>(`/quizzes/${quizId}/attempts`);
    setAttempts((prev) => ({ ...prev, [quizId]: quizAttempts }));
    return quizAttempts;
  };

  return { quizzes, attempts, loading, loadAttempts };
}
