'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Pencil, Trash2, X, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { useSubjects } from '@/hooks/useSubjects';

const COLORS = [
  '#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6',
  '#ec4899', '#06b6d4', '#f97316', '#6366f1', '#14b8a6',
];

export function SubjectsManager() {
  const router = useRouter();
  const { subjects, loading, createSubject, updateSubject, deleteSubject } = useSubjects();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingSubject, setEditingSubject] = useState<{ id: string; name: string; description?: string; color: string } | null>(null);
  const [isDeleting, setIsDeleting] = useState<string | null>(null);

  const handleCreate = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const name = formData.get('name') as string;
    const description = formData.get('description') as string;
    const color = formData.get('color') as string;
    
    if (!name.trim()) return;
    
    await createSubject(name, description || undefined, color);
    setIsCreateOpen(false);
    (e.target as HTMLFormElement).reset();
  };

  const handleUpdate = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!editingSubject) return;
    
    const formData = new FormData(e.currentTarget);
    const name = formData.get('name') as string;
    const description = formData.get('description') as string;
    const color = formData.get('color') as string;
    
    if (!name.trim()) return;
    
    await updateSubject(editingSubject.id, { name, description: description || undefined, color });
    setEditingSubject(null);
  };

  const handleDelete = async (id: string) => {
    setIsDeleting(id);
    await deleteSubject(id);
    setIsDeleting(null);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-4">
        <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogTrigger asChild>
          <Button variant="ghost" size="sm" className="w-full justify-start gap-2 text-muted-foreground">
            <Plus className="h-4 w-4" />
            Add Subject
          </Button>
        </DialogTrigger>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New Subject</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Subject Name</Label>
              <Input id="name" name="name" placeholder="e.g. Mathematics" required />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Description (optional)</Label>
              <Textarea id="description" name="description" placeholder="Brief description..." />
            </div>
            <div className="space-y-2">
              <Label>Color</Label>
              <div className="flex flex-wrap gap-2">
                {COLORS.map((color) => (
                  <label key={color} className="cursor-pointer">
                    <input
                      type="radio"
                      name="color"
                      value={color}
                      defaultChecked={color === COLORS[0]}
                      className="sr-only peer"
                    />
                    <div className="h-8 w-8 rounded-full border-2 border-transparent peer-checked:border-primary peer-checked:ring-2 peer-checked:ring-primary/20 transition-all" style={{ backgroundColor: color }} />
                  </label>
                ))}
              </div>
            </div>
            <Button type="submit" className="w-full">Create Subject</Button>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog open={!!editingSubject} onOpenChange={(open) => !open && setEditingSubject(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Subject</DialogTitle>
          </DialogHeader>
          {editingSubject && (
            <form onSubmit={handleUpdate} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="edit-name">Subject Name</Label>
                <Input
                  id="edit-name"
                  name="name"
                  defaultValue={editingSubject.name}
                  placeholder="e.g. Mathematics"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-description">Description (optional)</Label>
                <Textarea
                  id="edit-description"
                  name="description"
                  defaultValue={editingSubject.description}
                  placeholder="Brief description..."
                />
              </div>
              <div className="space-y-2">
                <Label>Color</Label>
                <div className="flex flex-wrap gap-2">
                  {COLORS.map((color) => (
                    <label key={color} className="cursor-pointer">
                      <input
                        type="radio"
                        name="color"
                        value={color}
                        defaultChecked={color === editingSubject.color}
                        className="sr-only peer"
                      />
                      <div className="h-8 w-8 rounded-full border-2 border-transparent peer-checked:border-primary peer-checked:ring-2 peer-checked:ring-primary/20 transition-all" style={{ backgroundColor: color }} />
                    </label>
                  ))}
                </div>
              </div>
              <Button type="submit" className="w-full">Save Changes</Button>
            </form>
          )}
        </DialogContent>
      </Dialog>

      {subjects.map((subject) => (
        <motion.div
          key={subject.id}
          layout
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -10 }}
          className="group flex items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-accent/50"
        >
          <div className="h-2 w-2 shrink-0 rounded-full" style={{ backgroundColor: subject.color }} />
          <span
            className="flex-1 truncate text-sm cursor-pointer hover:text-primary"
            onClick={() => router.push(`/subjects/${subject.id}`)}
          >{subject.name}</span>
          <div className="flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
            <Button
              variant="ghost"
              size="icon"
              className="h-6 w-6"
              onClick={() => setEditingSubject({ id: subject.id, name: subject.name, description: subject.description, color: subject.color })}
            >
              <Pencil className="h-3 w-3" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="h-6 w-6 text-red-500 hover:text-red-500"
              onClick={() => handleDelete(subject.id)}
              disabled={isDeleting === subject.id}
            >
              {isDeleting === subject.id ? (
                <Loader2 className="h-3 w-3 animate-spin" />
              ) : (
                <Trash2 className="h-3 w-3" />
              )}
            </Button>
          </div>
        </motion.div>
      ))}
    </div>
  );
}
