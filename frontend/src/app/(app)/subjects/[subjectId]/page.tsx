"use client";

import React, { useState } from "react";
import { motion } from "framer-motion";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ScrollArea } from "@/components/ui/scroll-area";
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
} from "lucide-react";
import Link from "next/link";
import {
  mockSubjects,
  mockTopicMastery,
  mockMaterials,
  mockChatHistory,
  mockQuizzes,
  mockQuizQuestions,
} from "@/lib/mock-data";
import { RadarChart, PolarGrid, PolarAngleAxis, Radar, ResponsiveContainer } from "recharts";

interface SubjectPageProps {
  params: Promise<{ subjectId: string }>;
}

export default function SubjectPage({ params }: SubjectPageProps) {
  const [resolvedParams, setResolvedParams] = useState<{ subjectId: string } | null>(null);

  React.useEffect(() => {
    params.then((p) => setResolvedParams(p));
  }, [params]);

  const subjectId = resolvedParams?.subjectId || "1";
  const subject = mockSubjects.find((s) => s.id === subjectId) || mockSubjects[0];
  const topics = mockTopicMastery[subjectId] || [];
  const materials = mockMaterials.filter((m) => m.subjectId === subjectId);
  const quizzes = mockQuizzes.filter((q) => q.subjectId === subjectId);
  const [messages, setMessages] = useState(mockChatHistory);
  const [input, setInput] = useState("");

  const handleSend = () => {
    if (!input.trim()) return;
    const userMsg = {
      id: `u${Date.now()}`,
      role: "user" as const,
      content: input,
      timestamp: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setInput("");

    setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        {
          id: `a${Date.now()}`,
          role: "assistant" as const,
          content: `Based on your materials for ${subject.name}, I can help you understand this topic better. Would you like me to generate a practice quiz or explain a specific concept?`,
          timestamp: new Date().toISOString(),
          sources: materials.slice(0, 2).map((m) => m.name),
        },
      ]);
    }, 1000);
  };

  const avgConfidence = topics.length
    ? Math.round(topics.reduce((s, t) => s + t.confidence, 0) / topics.length)
    : 0;

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
          <p className="text-muted-foreground">
            {subject.code} &middot; {subject.mastery}% mastery &middot; Last studied {subject.lastStudied}
          </p>
        </div>
        <div className="ml-auto flex gap-2">
          <Link href={`/subjects/${subjectId}/quizzes/new`}>
            <Button variant="outline" className="gap-2">
              <Sparkles className="h-4 w-4" />
              Generate Quiz
            </Button>
          </Link>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Overall Mastery</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{subject.mastery}%</div>
            <Progress value={subject.mastery} className="mt-2 h-2" />
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Avg Topic Confidence</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{avgConfidence}%</div>
            <Progress value={avgConfidence} className="mt-2 h-2" />
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Quizzes Completed</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{quizzes.length}</div>
            <p className="mt-1 text-xs text-muted-foreground">
              {materials.length} materials uploaded
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
            <ScrollArea className="flex-1 p-4">
              <div className="space-y-4">
                {messages.map((msg) => (
                  <motion.div
                    key={msg.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
                  >
                    <div
                      className={`max-w-[80%] rounded-lg px-4 py-3 ${
                        msg.role === "user"
                          ? "bg-primary text-primary-foreground"
                          : "bg-muted"
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
              </div>
            </ScrollArea>
            <div className="border-t p-4">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleSend()}
                  placeholder={`Ask about ${subject.name}...`}
                  className="flex-1 rounded-lg border bg-background px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                />
                <Button onClick={handleSend} size="icon">
                  <Send className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </Card>
        </TabsContent>

        <TabsContent value="topics">
          <div className="grid gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Topic Mastery Radar</CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <RadarChart
                    data={topics.map((t) => ({
                      topic: t.topic.split(" ")[0],
                      confidence: t.confidence,
                    }))}
                  >
                    <PolarGrid className="stroke-border" />
                    <PolarAngleAxis dataKey="topic" className="text-xs" />
                    <Radar
                      name="Confidence"
                      dataKey="confidence"
                      stroke={subject.color}
                      fill={subject.color}
                      fillOpacity={0.3}
                    />
                  </RadarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Topics Breakdown</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {topics.map((topic) => (
                    <div key={topic.topic} className="space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium">{topic.topic}</span>
                        <span className="text-sm font-bold">{topic.confidence}%</span>
                      </div>
                      <Progress value={topic.confidence} className="h-2" />
                      <div className="flex items-center justify-between text-xs text-muted-foreground">
                        <span>{topic.questionsAttempted} questions</span>
                        <span>{topic.correctRate}% correct</span>
                      </div>
                    </div>
                  ))}
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
                {materials.length > 0 ? materials.map((material) => (
                  <div
                    key={material.id}
                    className="flex items-center gap-4 rounded-lg border p-4 transition-colors hover:bg-accent/50"
                  >
                    <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                      <FileText className="h-5 w-5 text-primary" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium truncate">{material.name}</p>
                      <p className="text-xs text-muted-foreground">
                        {material.size} &middot; Uploaded {material.uploadedAt}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <span
                        className={cn(
                          "rounded-full px-2 py-0.5 text-xs",
                          material.status === "ready"
                            ? "bg-emerald-500/10 text-emerald-500"
                            : material.status === "processing"
                            ? "bg-amber-500/10 text-amber-500"
                            : "bg-red-500/10 text-red-500"
                        )}
                      >
                        {material.status}
                      </span>
                      <Button variant="ghost" size="icon">
                        <Play className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                )) : (
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
                {quizzes.length > 0 ? quizzes.map((quiz) => (
                  <Link
                    key={quiz.id}
                    href={`/subjects/${subjectId}/quizzes/${quiz.id}`}
                    className="flex items-center gap-4 rounded-lg border p-4 transition-colors hover:bg-accent/50"
                  >
                    <div
                      className={cn(
                        "flex h-10 w-10 items-center justify-center rounded-lg",
                        quiz.score >= 80
                          ? "bg-emerald-500/10"
                          : quiz.score >= 60
                          ? "bg-amber-500/10"
                          : "bg-red-500/10"
                      )}
                    >
                      <span
                        className={cn(
                          "text-sm font-bold",
                          quiz.score >= 80
                            ? "text-emerald-500"
                            : quiz.score >= 60
                            ? "text-amber-500"
                            : "text-red-500"
                        )}
                      >
                        {quiz.score}%
                      </span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium">{quiz.title}</p>
                      <p className="text-xs text-muted-foreground">
                        {quiz.totalQuestions} questions &middot; {quiz.duration} min &middot; {quiz.date}
                      </p>
                    </div>
                    <ChevronRight className="h-4 w-4 text-muted-foreground" />
                  </Link>
                )) : (
                  <div className="py-12 text-center">
                    <QuizIcon className="mx-auto h-12 w-12 text-muted-foreground/50" />
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

function cn(...classes: (string | boolean | undefined | null)[]) {
  return classes.filter(Boolean).join(" ");
}
