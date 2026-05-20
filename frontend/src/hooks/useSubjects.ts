'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api/client';
import type { Subject } from '@/types/api';

export function useSubjects() {
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<Subject[]>('/subjects')
      .then(setSubjects)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return { subjects, loading, error };
}
