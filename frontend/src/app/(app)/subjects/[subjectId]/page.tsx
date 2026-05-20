'use client';

import React, { useState, useEffect, useRef } from 'react';
import { motion } from 'framer-motion';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  BookOpen,
  MessageSquare,
  BarChart3,
  FileText,
  Sparkles,
  Send,
  HelpCircle,
  Clock,
  CheckCircle2,
  XCircle,
  ChevronRight,
  Play,
  Loader2,
} from 'lucide-react';
import Link from 'next/link';
import { useRequireAuth } from '@/hooks/useRequireAuth';
import { useTopicMastery } from '@/hooks/useTopicMastery';
import { useMaterials } from '@/hooks/useMaterials';
import { useQuizzes } from '@/hooks/useQuizzes';
import { api } from '@/lib/api/client';
import { cn } from '@/lib/utils';

interface SubjectPageProps {
  params: Promise<{ subjectId: string }>;
}

export default function SubjectPage({ params }: SubjectPageProps) {
  const [resolvedParams, setResolvedParams] = useState<{ subjectId: string } | null>(null);
  const { user } = useRequireAuth();
  const [messages, setMessages] = useState<{ id: string; role: 'user' | 'assistant'; content: string; sources?: string[] }[]>([]);
  const [input, setInput] = useState('');
  const [isSending, setIsSending] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    params.then((p) => setResolvedParams(p));
  }, [params]);

  const subjectId = resolvedParams?.subjectId || '';
  const { topics, loading: topicsLoading } = useTopicMastery(subjectId);
  const { materials, loading: materialsLoading } = useMaterials(subjectId);
  const { quizzes, attempts, loading: quizzesLoading, loadAttempts } = useQuizzes(subjectId);

  const [subject, setSubject] = useState<{ id: string; name: string; color: string } | null>(null);

  useEffect(() => {
    if (!subjectId || !user) return;
    api.get<any[]>('/subjects')
      .then((subjects) => {
        const found = subjects.find((s: any) => s.id === subjectId);
        if (found) setSubject(found);
      })
      .catch(console.error);
  }, [subjectId, user]);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || !subjectId || isSending) return;

    const userMsg = {
      id: `u${Date.now()}`,
      role: 'user' as const,
      content: input,
    };
    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setIsSending(true);

    try {
      const { message, sources } = await api.post<{ message: string; sources: string[] }>(
        `/subjects/${subjectId}/chat`,
        { message: input }
      );
      setMessages((prev) => [
        ...prev,
        { id: `a${Date.now()}`, role: 'assistant', content: message, sources },
      ]);
    } catch (err: any) {
      setMessages((prev) => [
        ...prev,
        { id: `a${Date.now()}`, role: 'assistant', content: `Error: ${err.message}` },
      ]);
    } finally {
      setIsSending(false);
    }
  };

  const avgConfidence = topics.length
    ? Math.round(topics.reduce((s, t) => s + t.confidence, 0) / topics.length)
    : 0;

  if (!subject) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <div
          className="flex h-12 w-12 items-center justify-center rounded-xl"
          style={{ backgroundColor: `${subject.color}20` }}
        >
          <BookOpen className="h-6 w-6" style={{ color: subject.color }} />
        </div>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">{subject.name}</h1>
          <p className="text-muted-foreground">{avgConfidence}% avg confidence</p>
        </div>
        <div className="ml-auto flex gap-2">
          <Button variant="outline" className="gap-2" onClick={() => alert('Quiz generation coming soon')}>
            <Sparkles className="h-4 w-4" />
            Generate Quiz
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Overall Mastery</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{avgConfidence}%</div>
            <Progress value={avgConfidence} className="mt-2 h-2" />
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Topics</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{topics.length}</div>
            <p className="mt-1 text-xs text-muted-foreground">
              {materials.length} materials uploaded
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Quizzes Completed</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{quizzes.length}</div>
            <p className="mt-1 text-xs text-muted-foreground">
              {quizzes.reduce((sum, q) => sum + (q.questionCount || 0), 0)} total questions
            </p>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="chat" className="space-y-4">
        <TabsList>
          <TabsTrigger value="chat" className="gap-2">
            <MessageSquare className="h-4 w-4" />
            AI Tutor
          </TabsTrigger>
          <TabsTrigger value="topics" className="gap-2">
            <BarChart3 className="h-4 w-4" />
            Topic Mastery
          </TabsTrigger>
          <TabsTrigger value="materials" className="gap-2">
            <FileText className="h-4 w-4" />
            Materials
          </TabsTrigger>
          <TabsTrigger value="quizzes" className="gap-2">
            <HelpCircle className="h-4 w-4" />
            Quiz History
          </TabsTrigger>
        </TabsList>

        <TabsContent value="chat">
          <Card className="h-[600px] flex flex-col">
            <CardHeader className="border-b">
              <CardTitle className="flex items-center gap-2">
                <Sparkles className="h-5 w-5 text-primary" />
                AI Tutor - {subject.name}
              </CardTitle>
            </CardHeader>
            <ScrollArea className="flex-1 p-4" ref={scrollRef as any}>
              <div className="space-y-4">
                {messages.map((msg) => (
                  <motion.div
                    key={msg.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
                  >
                    <div
                      className={`max-w-[80%] rounded-lg px-4 py-3 ${
                        msg.role === 'user'
                          ? 'bg-primary text-primary-foreground'
                          : 'bg-muted'
                      }`}
                    >
                      <p className="text-sm whitespace-pre-wrap">{msg.content}</p>
                      {msg.sources && msg.sources.length > 0 && (
                        <div className="mt-2 flex flex-wrap gap-1">
                          {msg.sources.map((source, i) => (
                            <span
                              key={i}
                              className="inline-flex items-center gap-1 rounded bg-primary/20 px-2 py-0.5 text-xs"
                            >
                              <FileText className="h-3 w-3" />
                              {source}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </motion.div>
                ))}
                {isSending && (
                  <div className="flex justify-start">
                    <div className="rounded-lg bg-muted px-4 py-3">
                      <Loader2 className="h-4 w-4 animate-spin" />
                    </div>
                  </div>
                )}
              </div>
            </ScrollArea>
            <div className="border-t p-4">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                  placeholder={`Ask about ${subject.name}...`}
                  className="flex-1 rounded-lg border bg-background px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                  disabled={isSending}
                />
                <Button onClick={handleSend} size="icon" disabled={isSending || !input.trim()}>
                  {isSending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                </Button>
              </div>
            </div>
          </Card>
        </TabsContent>

        <TabsContent value="topics">
          <div className="grid gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Topics Breakdown</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {topicsLoading ? (
                    <div className="flex justify-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin" />
                    </div>
                  ) : topics.length > 0 ? (
                    topics.map((topic) => (
                      <div key={topic.topic} className="space-y-2">
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium">{topic.topic}</span>
                          <span className="text-sm font-bold">{topic.confidence}%</span>
                        </div>
                        <Progress value={topic.confidence} className="h-2" />
                        <div className="flex items-center justify-between text-xs text-muted-foreground">
                          <span>{topic.questionsAttempted} questions</span>
                          <span>
                            {topic.questionsAttempted > 0
                              ? Math.round((topic.correctAnswers / topic.questionsAttempted) * 100)
                              : 0}
                            % correct
                          </span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="py-8 text-center text-sm text-muted-foreground">
                      No topics yet. Take quizzes to track your mastery.
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="materials">
          <Card>
            <CardHeader>
              <CardTitle>Learning Materials</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {materialsLoading ? (
                  <div className="flex justify-center py-8">
                    <Loader2 className="h-6 w-6 animate-spin" />
                  </div>
                ) : materials.length > 0 ? (
                  materials.map((material) => (
                    <div
                      key={material.id}
                      className="flex items-center gap-4 rounded-lg border p-4 transition-colors hover:bg-accent/50"
                    >
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <FileText className="h-5 w-5 text-primary" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{material.fileName}</p>
                        <p className="text-xs text-muted-foreground">
                          {material.fileType} &middot; Uploaded {new Date(material.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <span
                          className={cn(
                            'rounded-full px-2 py-0.5 text-xs',
                            material.status === 'PROCESSED'
                              ? 'bg-emerald-500/10 text-emerald-500'
                              : material.status === 'PROCESSING'
                              ? 'bg-amber-500/10 text-amber-500'
                              : material.status === 'FAILED'
                              ? 'bg-red-500/10 text-red-500'
                              : 'bg-blue-500/10 text-blue-500'
                          )}
                        >
                          {material.status.toLowerCase()}
                        </span>
                        <Button variant="ghost" size="icon">
                          <Play className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="py-12 text-center">
                    <FileText className="mx-auto h-12 w-12 text-muted-foreground/50" />
                    <p className="mt-2 text-sm text-muted-foreground">
                      No materials uploaded yet
                    </p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="quizzes">
          <Card>
            <CardHeader>
              <CardTitle>Quiz History</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {quizzesLoading ? (
                  <div className="flex justify-center py-8">
                    <Loader2 className="h-6 w-6 animate-spin" />
                  </div>
                ) : quizzes.length > 0 ? (
                  quizzes.map((quiz) => (
                    <Link
                      key={quiz.id}
                      href={`/subjects/${subjectId}/quizzes/${quiz.id}`}
                      className="flex items-center gap-4 rounded-lg border p-4 transition-colors hover:bg-accent/50"
                    >
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <HelpCircle className="h-5 w-5 text-primary" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium">{quiz.title}</p>
                        <p className="text-xs text-muted-foreground">
                          {quiz.questionCount} questions
                        </p>
                      </div>
                      <ChevronRight className="h-4 w-4 text-muted-foreground" />
                    </Link>
                  ))
                ) : (
                  <div className="py-12 text-center">
                    <HelpCircle className="mx-auto h-12 w-12 text-muted-foreground/50" />
                    <p className="mt-2 text-sm text-muted-foreground">
                      No quizzes taken yet
                    </p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
