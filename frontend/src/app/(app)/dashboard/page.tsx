'use client';

import { useRequireAuth } from '@/hooks/useRequireAuth';
import { useSubjects } from '@/hooks/useSubjects';
import { useAnalytics } from '@/hooks/useAnalytics';
import { TaskManager } from '@/components/task-manager';
import { motion } from 'framer-motion';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import {
  TrendingUp,
  BookOpen,
  Clock,
  Target,
  AlertTriangle,
  Sparkles,
  ArrowRight,
  Loader2,
} from 'lucide-react';
import Link from 'next/link';
import { cn } from '@/lib/utils';

const container = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.05 } },
};

const item = {
  hidden: { opacity: 0, y: 10 },
  show: { opacity: 1, y: 0 },
};

export default function DashboardPage() {
  const { user, loading: authLoading } = useRequireAuth();
  const { subjects, loading: subjectsLoading } = useSubjects();
  const { overview, weakestTopics, loading: analyticsLoading } = useAnalytics();

  const loading = authLoading || subjectsLoading || analyticsLoading;

  if (loading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const masteredCount = subjects.filter((s) => (overview?.avgConfidence ?? 0) >= 80).length;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            Welcome back, {user?.displayName?.split(' ')[0] || 'Student'}
          </h1>
          <p className="text-muted-foreground">
            {overview?.studyStreak ?? 0} day study streak
          </p>
        </div>
        <Link
          href="/analytics"
          className="inline-flex items-center gap-2 rounded-lg bg-primary/10 px-4 py-2 text-sm font-medium text-primary transition-colors hover:bg-primary/20"
        >
          View Analytics
          <ArrowRight className="h-4 w-4" />
        </Link>
      </div>

      <motion.div variants={container} initial="hidden" animate="show" className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Avg Confidence</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{overview?.avgConfidence ?? 0}%</div>
              <div className="flex items-center gap-1 text-xs text-emerald-500">
                <TrendingUp className="h-3 w-3" />
                {overview?.topicsMastered ?? 0} topics mastered
              </div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Study Streak</CardTitle>
              <Clock className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{overview?.studyStreak ?? 0} days</div>
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                Keep it going!
              </div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Subjects</CardTitle>
              <BookOpen className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{subjects.length}</div>
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                {masteredCount} mastered
              </div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Weak Topics</CardTitle>
              <AlertTriangle className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{weakestTopics.length}</div>
              <div className="flex items-center gap-1 text-xs text-amber-500">
                Need attention
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </motion.div>

      <div className="grid gap-4 lg:grid-cols-7">
        <motion.div variants={container} initial="hidden" animate="show" className="lg:col-span-4">
          <Card>
            <CardHeader>
              <CardTitle>Tasks</CardTitle>
            </CardHeader>
            <CardContent>
              <TaskManager />
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={container} initial="hidden" animate="show" className="lg:col-span-3">
          <Card>
            <CardHeader>
              <CardTitle>Weak Topics</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {weakestTopics.slice(0, 4).map((topic) => (
                  <div key={topic.topic} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium">{topic.topic}</span>
                      <span className="text-xs text-muted-foreground">{topic.confidence}%</span>
                    </div>
                    <Progress value={topic.confidence} className="h-2" />
                  </div>
                ))}
                {weakestTopics.length === 0 && (
                  <div className="py-8 text-center text-sm text-muted-foreground">
                    No weak topics identified
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>

      <motion.div variants={container} initial="hidden" animate="show">
        <Card>
          <CardHeader>
            <CardTitle>Subject Mastery</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {subjects.map((subject) => (
                <Link
                  key={subject.id}
                  href={`/subjects/${subject.id}`}
                  className="group rounded-lg border p-4 transition-all hover:border-primary/50 hover:shadow-sm"
                >
                  <div className="flex items-center gap-3">
                    <div
                      className="flex h-10 w-10 items-center justify-center rounded-lg"
                      style={{ backgroundColor: `${subject.color}20` }}
                    >
                      <BookOpen className="h-5 w-5" style={{ color: subject.color }} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium truncate">{subject.name}</p>
                    </div>
                  </div>
                  <div className="mt-3 space-y-1">
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-muted-foreground">Confidence</span>
                      <span className="font-medium">
                        {overview?.avgConfidence ?? 0}%
                      </span>
                    </div>
                    <Progress value={overview?.avgConfidence ?? 0} className="h-1.5" />
                  </div>
                </Link>
              ))}
              {subjects.length === 0 && (
                <div className="col-span-full py-12 text-center">
                  <BookOpen className="mx-auto mb-2 h-12 w-12 text-muted-foreground/50" />
                  <p className="text-sm text-muted-foreground">
                    No subjects yet. Create your first subject to get started.
                  </p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </motion.div>
    </div>
  );
}
