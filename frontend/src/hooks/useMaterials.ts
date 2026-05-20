'use client';

import { useEffect, useState, useCallback } from 'react';
import { api } from '@/lib/api/client';
import type { Material } from '@/types/api';

export function useMaterials(subjectId: string) {
  const [materials, setMaterials] = useState<Material[]>([]);
  const [loading, setLoading] = useState(true);

  const loadMaterials = useCallback(() => {
    if (!subjectId) return;
    api.get<Material[]>(`/subjects/${subjectId}/materials`)
      .then(setMaterials)
      .finally(() => setLoading(false));
  }, [subjectId]);

  useEffect(() => {
    loadMaterials();
  }, [loadMaterials]);

  const uploadMaterial = async (file: File, onProgress?: (progress: number) => void) => {
    if (!subjectId) throw new Error('Subject ID is required');
    
    const formData = new FormData();
    formData.append('file', file);

    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
    const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

    return new Promise<Material>((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      
      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable && onProgress) {
          onProgress(Math.round((e.loaded / e.total) * 100));
        }
      });

      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          const response = JSON.parse(xhr.responseText);
          resolve(response.data);
        } else {
          reject(new Error(`Upload failed: ${xhr.status}`));
        }
      });

      xhr.addEventListener('error', () => reject(new Error('Upload failed')));

      xhr.open('POST', `${API_BASE}/subjects/${subjectId}/materials/upload`);
      if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      xhr.send(formData);
    });
  };

  return { materials, loading, uploadMaterial, refresh: loadMaterials };
}
