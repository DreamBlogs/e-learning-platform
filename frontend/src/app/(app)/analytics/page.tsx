'use client';

import { useRequireAuth } from '@/hooks/useRequireAuth';
import { useAnalytics } from '@/hooks/useAnalytics';
import { motion } from 'framer-motion';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  TrendingUp,
  Target,
  AlertTriangle,
  CheckCircle2,
  Loader2,
} from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  Radar,
  BarChart,
  Bar,
} from 'recharts';
import { cn } from '@/lib/utils';

const container = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.05 } },
};

const item = {
  hidden: { opacity: 0, y: 10 },
  show: { opacity: 1, y: 0 },
};

export default function AnalyticsPage() {
  const { user } = useRequireAuth();
  const { overview, subjectAnalytics, weakestTopics, strongestTopics, loading } = useAnalytics();

  if (loading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const radarData = subjectAnalytics.map((s) => ({
    subject: s.code,
    confidence: s.avgConfidence,
  }));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Learning Analytics</h1>
        <p className="text-muted-foreground">
          Comprehensive view of your learning progress and knowledge mastery
        </p>
      </div>

      <motion.div variants={container} initial="hidden" animate="show" className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Avg Confidence</CardTitle>
              <Target className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{overview?.avgConfidence ?? 0}%</div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Topics Mastered</CardTitle>
              <CheckCircle2 className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{overview?.topicsMastered ?? 0}</div>
              <p className="text-xs text-muted-foreground">
                out of {overview?.totalTopics ?? 0} total topics
              </p>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Weakest Area</CardTitle>
              <AlertTriangle className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold truncate">
                {overview?.weakestArea?.topic ?? 'N/A'}
              </div>
              <p className="text-xs text-red-500">
                {overview?.weakestArea?.confidence ?? 0}% confidence
              </p>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Study Streak</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{overview?.studyStreak ?? 0} days</div>
            </CardContent>
          </Card>
        </motion.div>
      </motion.div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="topics">Topic Analysis</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <div className="grid gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Subject Comparison</CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <RadarChart data={radarData}>
                    <PolarGrid className="stroke-border" />
                    <PolarAngleAxis dataKey="subject" className="text-xs" />
                    <Radar
                      name="Confidence"
                      dataKey="confidence"
                      stroke="hsl(var(--chart-1))"
                      fill="hsl(var(--chart-1))"
                      fillOpacity={0.3}
                    />
                  </RadarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Weakest Topics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {weakestTopics.length > 0 ? (
                    weakestTopics.map((topic) => (
                      <div key={topic.topic} className="space-y-2">
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium">{topic.topic}</span>
                          <span className="text-sm text-red-500">{topic.confidence}%</span>
                        </div>
                        <Progress value={topic.confidence} className="h-2" />
                      </div>
                    ))
                  ) : (
                    <p className="py-8 text-center text-sm text-muted-foreground">
                      No data yet. Take quizzes to see your weak topics.
                    </p>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="topics">
          <Card>
            <CardHeader>
              <CardTitle>Topic Confidence by Subject</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                {subjectAnalytics.map((subject) => (
                  <div key={subject.id}>
                    <h3 className="mb-3 flex items-center gap-2 text-sm font-medium">
                      <div
                        className="h-3 w-3 rounded-full"
                        style={{ backgroundColor: subject.color }}
                      />
                      {subject.name}
                    </h3>
                    <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                      {subjectAnalytics
                        .filter((s) => s.id === subject.id)
                        .map((s) => (
                          <div
                            key={s.id}
                            className={cn(
                              'rounded-lg border p-3 text-center',
                              s.avgConfidence >= 80
                                ? 'bg-emerald-500/10'
                                : s.avgConfidence >= 60
                                ? 'bg-amber-500/10'
                                : 'bg-red-500/10'
                            )}
                          >
                            <p className="text-sm font-medium">{s.name}</p>
                            <p
                              className={cn(
                                'text-lg font-bold',
                                s.avgConfidence >= 80
                                  ? 'text-emerald-500'
                                  : s.avgConfidence >= 60
                                  ? 'text-amber-500'
                                  : 'text-red-500'
                              )}
                            >
                              {s.avgConfidence}%
                            </p>
                          </div>
                        ))}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
