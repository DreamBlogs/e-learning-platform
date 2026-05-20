'use client';

import { useEffect, useState, useCallback } from 'react';
import { api } from '@/lib/api/client';
import type { Quiz, QuizAttempt, QuizQuestion } from '@/types/api';

export function useQuizzes(subjectId: string) {
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [attempts, setAttempts] = useState<Record<string, QuizAttempt[]>>({});
  const [loading, setLoading] = useState(true);

  const loadQuizzes = useCallback(() => {
    if (!subjectId) return;
    api.get<Quiz[]>(`/subjects/${subjectId}/quizzes`)
      .then(setQuizzes)
      .finally(() => setLoading(false));
  }, [subjectId]);

  useEffect(() => {
    loadQuizzes();
  }, [loadQuizzes]);

  const loadAttempts = async (quizId: string) => {
    const quizAttempts = await api.get<QuizAttempt[]>(`/quizzes/${quizId}/attempts`);
    setAttempts((prev) => ({ ...prev, [quizId]: quizAttempts }));
    return quizAttempts;
  };

  const createQuiz = async (title: string, description?: string, timeLimitMinutes?: number) => {
    if (!subjectId) throw new Error('Subject ID is required');
    const newQuiz = await api.post<Quiz>(`/subjects/${subjectId}/quizzes`, { title, description, timeLimitMinutes });
    setQuizzes((prev) => [...prev, newQuiz]);
    return newQuiz;
  };

  const addQuestion = async (quizId: string, question: Omit<QuizQuestion, 'id' | 'quizId'>) => {
    const newQuestion = await api.post<QuizQuestion>(`/quizzes/${quizId}/questions`, question);
    return newQuestion;
  };

  return { quizzes, attempts, loading, loadAttempts, createQuiz, addQuestion, refresh: loadQuizzes };
}
