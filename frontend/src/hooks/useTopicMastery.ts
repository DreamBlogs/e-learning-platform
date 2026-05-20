'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api/client';
import type { TopicMastery } from '@/types/api';

export function useTopicMastery(subjectId: string) {
  const [topics, setTopics] = useState<TopicMastery[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!subjectId) return;
    api.get<TopicMastery[]>(`/subjects/${subjectId}/topics`)
      .then(setTopics)
      .finally(() => setLoading(false));
  }, [subjectId]);

  return { topics, loading };
}
