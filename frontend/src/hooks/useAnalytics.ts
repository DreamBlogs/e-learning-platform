'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api/client';
import type { QuizAttempt, SubjectAnalytics, AnalyticsOverview, TopicMastery } from '@/types/api';

export function useAnalytics() {
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [subjectAnalytics, setSubjectAnalytics] = useState<SubjectAnalytics[]>([]);
  const [weakestTopics, setWeakestTopics] = useState<TopicMastery[]>([]);
  const [strongestTopics, setStrongestTopics] = useState<TopicMastery[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get<AnalyticsOverview>('/analytics/overview'),
      api.get<SubjectAnalytics[]>('/analytics/subjects'),
      api.get<TopicMastery[]>('/analytics/weakest-topics?limit=4'),
      api.get<TopicMastery[]>('/analytics/strongest-topics?limit=4'),
    ])
      .then(([overview, subjects, weakest, strongest]) => {
        setOverview(overview);
        setSubjectAnalytics(subjects);
        setWeakestTopics(weakest);
        setStrongestTopics(strongest);
      })
      .finally(() => setLoading(false));
  }, []);

  return { overview, subjectAnalytics, weakestTopics, strongestTopics, loading };
}
