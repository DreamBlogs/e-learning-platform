'use client';

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Trash2, Loader2, Sparkles, ChevronRight, Check } from 'lucide-react';
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
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { useQuizzes } from '@/hooks/useQuizzes';
import type { QuizQuestion } from '@/types/api';

interface QuizCreatorProps {
  subjectId: string;
}

interface NewQuestion {
  questionText: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctAnswer: 'A' | 'B' | 'C' | 'D';
  topic?: string;
  difficulty: 'easy' | 'medium' | 'hard';
}

export function QuizCreator({ subjectId }: QuizCreatorProps) {
  const { quizzes, loading, createQuiz, addQuestion } = useQuizzes(subjectId);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [selectedQuiz, setSelectedQuiz] = useState<string | null>(null);
  const [questions, setQuestions] = useState<NewQuestion[]>([]);
  const [isAddingQuestion, setIsAddingQuestion] = useState(false);
  const [savingQuestions, setSavingQuestions] = useState(false);

  const handleCreateQuiz = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsCreating(true);
    
    const formData = new FormData(e.currentTarget);
    const title = formData.get('title') as string;
    const description = formData.get('description') as string;
    const timeLimitMinutes = parseInt(formData.get('timeLimitMinutes') as string) || undefined;

    if (!title.trim()) {
      setIsCreating(false);
      return;
    }

    try {
      const newQuiz = await createQuiz(title, description || undefined, timeLimitMinutes);
      setSelectedQuiz(newQuiz.id);
      setQuestions([]);
    } finally {
      setIsCreating(false);
    }
  };

  const addNewQuestion = () => {
    setQuestions((prev) => [
      ...prev,
      {
        questionText: '',
        optionA: '',
        optionB: '',
        optionC: '',
        optionD: '',
        correctAnswer: 'A',
        difficulty: 'medium',
      },
    ]);
  };

  const updateQuestion = (index: number, updates: Partial<NewQuestion>) => {
    setQuestions((prev) => prev.map((q, i) => (i === index ? { ...q, ...updates } : q)));
  };

  const removeQuestion = (index: number) => {
    setQuestions((prev) => prev.filter((_, i) => i !== index));
  };

  const saveQuestions = async () => {
    if (!selectedQuiz || questions.length === 0) return;
    
    setSavingQuestions(true);
    try {
      for (let i = 0; i < questions.length; i++) {
        const q = questions[i];
        await addQuestion(selectedQuiz, {
          questionText: q.questionText,
          optionA: q.optionA,
          optionB: q.optionB,
          optionC: q.optionC,
          optionD: q.optionD,
          correctOption: q.correctAnswer,
          explanation: undefined,
          topic: q.topic,
          difficulty: q.difficulty,
          position: i + 1,
        });
      }
      setQuestions([]);
      setSelectedQuiz(null);
    } finally {
      setSavingQuestions(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-medium">Quizzes</h3>
        <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
          <DialogTrigger asChild>
            <Button size="sm">
              <Sparkles className="mr-1 h-4 w-4" />
              Create Quiz
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Quiz</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleCreateQuiz} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="quiz-title">Quiz Title</Label>
                <Input id="quiz-title" name="title" placeholder="e.g. Chapter 1 Quiz" required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="quiz-description">Description (optional)</Label>
                <Textarea id="quiz-description" name="description" placeholder="Quiz description..." />
              </div>
              <div className="space-y-2">
                <Label htmlFor="time-limit">Time Limit (minutes, optional)</Label>
                <Input id="time-limit" name="timeLimitMinutes" type="number" min="1" placeholder="30" />
              </div>
              <Button type="submit" className="w-full" disabled={isCreating}>
                {isCreating ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Plus className="mr-2 h-4 w-4" />}
                Create & Add Questions
              </Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <AnimatePresence>
        {selectedQuiz && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="overflow-hidden"
          >
            <div className="rounded-lg border p-4 space-y-4">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-medium">Add Questions</h4>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" onClick={() => { setSelectedQuiz(null); setQuestions([]); }}>
                    Cancel
                  </Button>
                  <Button size="sm" onClick={saveQuestions} disabled={savingQuestions || questions.length === 0}>
                    {savingQuestions ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Check className="mr-2 h-4 w-4" />}
                    Save All ({questions.length})
                  </Button>
                </div>
              </div>

              <div className="space-y-4 max-h-[400px] overflow-y-auto">
                {questions.map((q, index) => (
                  <motion.div
                    key={index}
                    layout
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="rounded-lg border bg-accent/30 p-4 space-y-3"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-medium text-muted-foreground">Question {index + 1}</span>
                      <Button variant="ghost" size="icon" className="h-6 w-6 text-red-500" onClick={() => removeQuestion(index)}>
                        <Trash2 className="h-3 w-3" />
                      </Button>
                    </div>
                    <Input
                      placeholder="Question text"
                      value={q.questionText}
                      onChange={(e) => updateQuestion(index, { questionText: e.target.value })}
                    />
                    <div className="grid grid-cols-2 gap-2">
                      {(['A', 'B', 'C', 'D'] as const).map((opt) => (
                        <div key={opt} className="flex items-center gap-2">
                          <input
                            type="radio"
                            name={`correct-${index}`}
                            checked={q.correctAnswer === opt}
                            onChange={() => updateQuestion(index, { correctAnswer: opt })}
                            className="h-4 w-4"
                          />
                          <Input
                            placeholder={`Option ${opt}`}
                            value={q[`option${opt}` as keyof NewQuestion] as string}
                            onChange={(e) => updateQuestion(index, { [`option${opt}`]: e.target.value } as Partial<NewQuestion>)}
                            className={q.correctAnswer === opt ? 'border-emerald-500' : ''}
                          />
                        </div>
                      ))}
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <Input
                        placeholder="Topic (optional)"
                        value={q.topic || ''}
                        onChange={(e) => updateQuestion(index, { topic: e.target.value })}
                      />
                      <Select
                        value={q.difficulty}
                        onValueChange={(v) => updateQuestion(index, { difficulty: v as NewQuestion['difficulty'] })}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="easy">Easy</SelectItem>
                          <SelectItem value="medium">Medium</SelectItem>
                          <SelectItem value="hard">Hard</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </motion.div>
                ))}
              </div>

              <Button variant="outline" className="w-full" onClick={addNewQuestion}>
                <Plus className="mr-2 h-4 w-4" />
                Add Question
              </Button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="space-y-2">
        {loading ? (
          <div className="flex justify-center py-8">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        ) : quizzes.length > 0 ? (
          quizzes.map((quiz) => (
            <motion.div
              key={quiz.id}
              layout
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex items-center gap-4 rounded-lg border p-4 transition-colors hover:bg-accent/50 cursor-pointer"
              onClick={() => window.location.href = `/subjects/${subjectId}/quizzes/${quiz.id}`}
            >
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                <Sparkles className="h-5 w-5 text-primary" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium">{quiz.title}</p>
                <p className="text-xs text-muted-foreground">
                  {quiz.questionCount} questions
                  {quiz.timeLimitMinutes && ` · ${quiz.timeLimitMinutes} min`}
                </p>
              </div>
              <ChevronRight className="h-4 w-4 text-muted-foreground" />
            </motion.div>
          ))
        ) : (
          <div className="py-8 text-center text-sm text-muted-foreground">
            No quizzes yet. Create your first quiz!
          </div>
        )}
      </div>
    </div>
  );
}
