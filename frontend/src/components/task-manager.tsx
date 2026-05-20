'use client';

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Trash2, Loader2, Calendar, Flag } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useTasks } from '@/hooks/useTasks';
import type { Task } from '@/types/api';
import { cn } from '@/lib/utils';

export function TaskManager() {
  const { tasks, loading, toggleTask, createTask, deleteTask } = useTasks();
  const [showForm, setShowForm] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const handleCreate = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsCreating(true);
    
    const formData = new FormData(e.currentTarget);
    const title = formData.get('title') as string;
    const description = formData.get('description') as string;
    const dueDate = formData.get('dueDate') as string;
    const priority = formData.get('priority') as Task['priority'];

    if (!title.trim()) {
      setIsCreating(false);
      return;
    }

    try {
      await createTask({
        title,
        description: description || undefined,
        dueDate: dueDate || undefined,
        priority,
      });
      setShowForm(false);
      (e.target as HTMLFormElement).reset();
    } finally {
      setIsCreating(false);
    }
  };

  const handleDelete = async (id: string) => {
    setDeletingId(id);
    await deleteTask(id);
    setDeletingId(null);
  };

  const pendingTasks = tasks.filter((t) => !t.completed);
  const completedTasks = tasks.filter((t) => t.completed);

  if (loading) {
    return (
      <div className="flex justify-center py-8">
        <Loader2 className="h-6 w-6 animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-medium">Tasks ({pendingTasks.length} pending)</h3>
        <Button variant="outline" size="sm" onClick={() => setShowForm(!showForm)}>
          <Plus className="mr-1 h-4 w-4" />
          {showForm ? 'Cancel' : 'Add Task'}
        </Button>
      </div>

      <AnimatePresence>
        {showForm && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="overflow-hidden"
          >
            <form onSubmit={handleCreate} className="space-y-3 rounded-lg border p-4">
              <div className="space-y-2">
                <Label htmlFor="title">Title</Label>
                <Input id="title" name="title" placeholder="Task title" required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="description">Description (optional)</Label>
                <Textarea id="description" name="description" placeholder="Task description..." />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label htmlFor="dueDate">Due Date</Label>
                  <Input id="dueDate" name="dueDate" type="date" />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="priority">Priority</Label>
                  <Select name="priority" defaultValue="medium">
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="low">Low</SelectItem>
                      <SelectItem value="medium">Medium</SelectItem>
                      <SelectItem value="high">High</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <Button type="submit" className="w-full" disabled={isCreating}>
                {isCreating ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Plus className="mr-2 h-4 w-4" />}
                Create Task
              </Button>
            </form>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="space-y-2">
        {pendingTasks.map((task) => (
          <motion.div
            key={task.id}
            layout
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="group flex items-start gap-3 rounded-lg border p-3 transition-colors hover:bg-accent/50"
          >
            <button
              onClick={() => toggleTask(task.id)}
              className="mt-0.5 h-4 w-4 shrink-0 rounded-full border-2 border-muted-foreground/30 hover:border-primary"
            />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium">{task.title}</p>
              {task.description && (
                <p className="text-xs text-muted-foreground mt-1">{task.description}</p>
              )}
              <div className="flex items-center gap-3 mt-2 text-xs text-muted-foreground">
                {task.dueDate && (
                  <span className="flex items-center gap-1">
                    <Calendar className="h-3 w-3" />
                    {new Date(task.dueDate).toLocaleDateString()}
                  </span>
                )}
                <span className={cn(
                  'flex items-center gap-1 rounded-full px-2 py-0.5',
                  task.priority === 'high'
                    ? 'bg-red-500/10 text-red-500'
                    : task.priority === 'medium'
                    ? 'bg-amber-500/10 text-amber-500'
                    : 'bg-blue-500/10 text-blue-500'
                )}>
                  <Flag className="h-3 w-3" />
                  {task.priority}
                </span>
              </div>
            </div>
            <Button
              variant="ghost"
              size="icon"
              className="h-7 w-7 shrink-0 opacity-0 transition-opacity group-hover:opacity-100 text-red-500 hover:text-red-500"
              onClick={() => handleDelete(task.id)}
              disabled={deletingId === task.id}
            >
              {deletingId === task.id ? (
                <Loader2 className="h-3 w-3 animate-spin" />
              ) : (
                <Trash2 className="h-3 w-3" />
              )}
            </Button>
          </motion.div>
        ))}

        {pendingTasks.length === 0 && !showForm && (
          <div className="py-8 text-center text-sm text-muted-foreground">
            No pending tasks
          </div>
        )}

        {completedTasks.length > 0 && (
          <div className="pt-4 border-t">
            <p className="text-xs text-muted-foreground mb-2">Completed ({completedTasks.length})</p>
            <div className="space-y-2">
              {completedTasks.map((task) => (
                <motion.div
                  key={task.id}
                  layout
                  className="flex items-center gap-3 rounded-lg border p-3 opacity-60"
                >
                  <button
                    onClick={() => toggleTask(task.id)}
                    className="mt-0.5 h-4 w-4 shrink-0 rounded-full border-2 border-emerald-500 bg-emerald-500"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm line-through">{task.title}</p>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 shrink-0 text-red-500 hover:text-red-500"
                    onClick={() => handleDelete(task.id)}
                    disabled={deletingId === task.id}
                  >
                    {deletingId === task.id ? (
                      <Loader2 className="h-3 w-3 animate-spin" />
                    ) : (
                      <Trash2 className="h-3 w-3" />
                    )}
                  </Button>
                </motion.div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
