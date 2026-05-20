'use client';

import { useState, useCallback, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, FileText, X, Loader2, CheckCircle2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { useMaterials } from '@/hooks/useMaterials';
import { cn } from '@/lib/utils';

interface MaterialsUploadProps {
  subjectId: string;
}

export function MaterialsUpload({ subjectId }: MaterialsUploadProps) {
  const { materials, loading, uploadMaterial, refresh } = useMaterials(subjectId);
  const [isDragging, setIsDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleUpload = async (file: File) => {
    if (file.type !== 'application/pdf') {
      setUploadError('Only PDF files are supported');
      return;
    }

    setUploading(true);
    setUploadProgress(0);
    setUploadError(null);

    try {
      await uploadMaterial(file, setUploadProgress);
      refresh();
    } catch (err: any) {
      setUploadError(err.message || 'Upload failed');
    } finally {
      setUploading(false);
      setUploadProgress(0);
    }
  };

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files[0];
    if (file) handleUpload(file);
  }, []);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) handleUpload(file);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleBrowseClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="space-y-4">
      <div
        className={cn(
          'rounded-lg border-2 border-dashed p-8 text-center transition-colors',
          isDragging ? 'border-primary bg-primary/5' : 'border-muted-foreground/25',
          uploading && 'pointer-events-none opacity-50'
        )}
        onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleDrop}
      >
        <Upload className={cn('mx-auto h-10 w-10', isDragging ? 'text-primary' : 'text-muted-foreground/50')} />
        <p className="mt-2 text-sm font-medium">
          {isDragging ? 'Drop PDF here' : 'Drag & drop PDF here'}
        </p>
        <p className="mt-1 text-xs text-muted-foreground">or</p>
        <Button variant="outline" size="sm" disabled={uploading} onClick={handleBrowseClick} className="mt-2">
          Browse Files
        </Button>
        <input ref={fileInputRef} type="file" accept=".pdf" onChange={handleFileSelect} className="hidden" />
      </div>

      <AnimatePresence>
        {uploading && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="space-y-2"
          >
            <div className="flex items-center justify-between text-xs">
              <span>Uploading...</span>
              <span>{uploadProgress}%</span>
            </div>
            <Progress value={uploadProgress} className="h-2" />
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {uploadError && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="flex items-center gap-2 rounded-lg bg-red-500/10 p-3 text-sm text-red-500"
          >
            <AlertCircle className="h-4 w-4 shrink-0" />
            {uploadError}
            <Button variant="ghost" size="icon" className="ml-auto h-5 w-5" onClick={() => setUploadError(null)}>
              <X className="h-3 w-3" />
            </Button>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="space-y-2">
        {loading ? (
          <div className="flex justify-center py-8">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        ) : materials.length > 0 ? (
          materials.map((material) => (
            <motion.div
              key={material.id}
              layout
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex items-center gap-4 rounded-lg border p-4"
            >
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                <FileText className="h-5 w-5 text-primary" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{material.fileName}</p>
                <p className="text-xs text-muted-foreground">
                  {material.fileType} &middot; {new Date(material.createdAt).toLocaleDateString()}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <span className={cn(
                  'flex items-center gap-1 rounded-full px-2 py-0.5 text-xs',
                  material.status === 'PROCESSED'
                    ? 'bg-emerald-500/10 text-emerald-500'
                    : material.status === 'PROCESSING'
                    ? 'bg-amber-500/10 text-amber-500'
                    : material.status === 'FAILED'
                    ? 'bg-red-500/10 text-red-500'
                    : 'bg-blue-500/10 text-blue-500'
                )}>
                  {material.status === 'PROCESSED' && <CheckCircle2 className="h-3 w-3" />}
                  {material.status === 'FAILED' && <AlertCircle className="h-3 w-3" />}
                  {material.status.toLowerCase()}
                </span>
              </div>
            </motion.div>
          ))
        ) : (
          <div className="py-8 text-center text-sm text-muted-foreground">
            No materials uploaded yet
          </div>
        )}
      </div>
    </div>
  );
}
