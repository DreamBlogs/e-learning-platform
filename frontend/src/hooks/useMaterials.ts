'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api/client';
import type { Material } from '@/types/api';

export function useMaterials(subjectId: string) {
  const [materials, setMaterials] = useState<Material[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!subjectId) return;
    api.get<Material[]>(`/subjects/${subjectId}/materials`)
      .then(setMaterials)
      .finally(() => setLoading(false));
  }, [subjectId]);

  return { materials, loading };
}
