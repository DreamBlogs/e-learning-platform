"use client";

import { motion } from "framer-motion";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  TrendingUp,
  TrendingDown,
  Brain,
  Target,
  Award,
  AlertTriangle,
  ArrowUpRight,
  ArrowDownRight,
} from "lucide-react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  Radar,
  LineChart,
  Line,
  ScatterChart,
  Scatter,
} from "recharts";
import {
  mockSubjects,
  mockTopicMastery,
  mockGPATrends,
  mockWeakTopics,
  mockActivityData,
} from "@/lib/mock-data";

const allTopics = Object.entries(mockTopicMastery).flatMap(([subjectId, topics]) =>
  topics.map((t) => ({
    ...t,
    subjectId,
    subjectName: mockSubjects.find((s) => s.id === subjectId)?.name || "",
  }))
);

const weakestTopics = [...allTopics].sort((a, b) => a.confidence - b.confidence).slice(0, 6);
const strongestTopics = [...allTopics].sort((a, b) => b.confidence - a.confidence).slice(0, 6);

const weeklyData = [
  { week: "W1", dsa: 72, math: 58, physics: 80, db: 88, stats: 45 },
  { week: "W2", dsa: 75, math: 62, physics: 82, db: 90, stats: 48 },
  { week: "W3", dsa: 78, math: 65, physics: 85, db: 91, stats: 52 },
  { week: "W4", dsa: 78, math: 65, physics: 82, db: 91, stats: 54 },
];

const consistencyData = mockActivityData.map((d, i) => ({
  day: d.day,
  minutes: d.minutes,
  goal: 60,
}));

const container = {
  hidden: { opacity: 0 },
  show: { opacity: 1, transition: { staggerChildren: 0.05 } },
};

const item = {
  hidden: { opacity: 0, y: 10 },
  show: { opacity: 1, y: 0 },
};

function ConfidenceColor({ value }: { value: number }) {
  const color =
    value >= 80 ? "text-emerald-500" : value >= 60 ? "text-amber-500" : "text-red-500";
  return <span className={color}>{value}%</span>;
}

export default function AnalyticsPage() {
  const avgConfidence = Math.round(
    allTopics.reduce((sum, t) => sum + t.confidence, 0) / allTopics.length
  );

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
              <Brain className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{avgConfidence}%</div>
              <div className="flex items-center gap-1 text-xs text-emerald-500">
                <ArrowUpRight className="h-3 w-3" />
                +5% from last week
              </div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Topics Mastered</CardTitle>
              <Award className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {allTopics.filter((t) => t.confidence >= 80).length}
              </div>
              <div className="text-xs text-muted-foreground">
                out of {allTopics.length} total topics
              </div>
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
                {weakestTopics[0]?.topic}
              </div>
              <div className="flex items-center gap-1 text-xs text-red-500">
                <ArrowDownRight className="h-3 w-3" />
                {weakestTopics[0]?.confidence}% confidence
              </div>
            </CardContent>
          </Card>
        </motion.div>

        <motion.div variants={item}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Study Consistency</CardTitle>
              <Target className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">87%</div>
              <div className="flex items-center gap-1 text-xs text-emerald-500">
                <ArrowUpRight className="h-3 w-3" />
                Above goal
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </motion.div>

      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="topics">Topic Analysis</TabsTrigger>
          <TabsTrigger value="trends">Trends</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>GPA Trend</CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={280}>
                  <LineChart data={mockGPATrends}>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                    <XAxis dataKey="month" className="text-xs" stroke="hsl(var(--muted-foreground))" />
                    <YAxis
                      className="text-xs"
                      stroke="hsl(var(--muted-foreground))"
                      domain={[3.0, 4.0]}
                    />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: "8px",
                      }}
                    />
                    <Line
                      type="monotone"
                      dataKey="gpa"
                      stroke="hsl(var(--chart-1))"
                      strokeWidth={2}
                      dot={{ fill: "hsl(var(--chart-1))" }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Subject Comparison</CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={280}>
                  <RadarChart
                    data={mockSubjects.map((s) => ({
                      subject: s.code,
                      mastery: s.mastery,
                    }))}
                  >
                    <PolarGrid className="stroke-border" />
                    <PolarAngleAxis dataKey="subject" className="text-xs" />
                    <Radar
                      name="Mastery"
                      dataKey="mastery"
                      stroke="hsl(var(--chart-1))"
                      fill="hsl(var(--chart-1))"
                      fillOpacity={0.3}
                    />
                  </RadarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Weakest Topics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {weakestTopics.map((topic) => (
                    <div key={topic.topic} className="space-y-2">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-sm font-medium">{topic.topic}</p>
                          <p className="text-xs text-muted-foreground">{topic.subjectName}</p>
                        </div>
                        <ConfidenceColor value={topic.confidence} />
                      </div>
                      <Progress value={topic.confidence} className="h-2" />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Strongest Topics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {strongestTopics.map((topic) => (
                    <div key={topic.topic} className="space-y-2">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-sm font-medium">{topic.topic}</p>
                          <p className="text-xs text-muted-foreground">{topic.subjectName}</p>
                        </div>
                        <ConfidenceColor value={topic.confidence} />
                      </div>
                      <Progress value={topic.confidence} className="h-2" />
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="topics" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Topic Confidence Heatmap</CardTitle>
              <p className="text-sm text-muted-foreground">
                Confidence levels across all subjects and topics
              </p>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {mockSubjects.map((subject) => (
                  <div key={subject.id} className="space-y-2">
                    <div className="flex items-center gap-2">
                      <span
                        className="h-3 w-3 rounded-full"
                        style={{ backgroundColor: subject.color }}
                      />
                      <span className="text-sm font-medium">{subject.code}</span>
                    </div>
                    <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
                      {(mockTopicMastery[subject.id] || []).map((topic) => (
                        <div
                          key={topic.topic}
                          className="rounded-lg border p-3 text-center transition-colors hover:bg-accent/50"
                          style={{
                            backgroundColor: `${subject.color}${Math.round(topic.confidence / 100 * 40).toString(16).padStart(2, "0")}`,
                          }}
                        >
                          <p className="text-xs font-medium truncate">{topic.topic}</p>
                          <p className="mt-1 text-lg font-bold">
                            <ConfidenceColor value={topic.confidence} />
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

        <TabsContent value="trends" className="space-y-4">
          <div className="grid gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Weekly Progress by Subject</CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={weeklyData}>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                    <XAxis dataKey="week" className="text-xs" stroke="hsl(var(--muted-foreground))" />
                    <YAxis className="text-xs" stroke="hsl(var(--muted-foreground))" domain={[0, 100]} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: "8px",
                      }}
                    />
                    <Line type="monotone" dataKey="dsa" stroke="#3b82f6" strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="math" stroke="#8b5cf6" strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="physics" stroke="#f59e0b" strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="db" stroke="#10b981" strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="stats" stroke="#ef4444" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Learning Consistency</CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={consistencyData}>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
                    <XAxis dataKey="day" className="text-xs" stroke="hsl(var(--muted-foreground))" />
                    <YAxis className="text-xs" stroke="hsl(var(--muted-foreground))" />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(var(--card))",
                        border: "1px solid hsl(var(--border))",
                        borderRadius: "8px",
                      }}
                    />
                    <Bar
                      dataKey="minutes"
                      fill="hsl(var(--chart-1))"
                      radius={[4, 4, 0, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}
