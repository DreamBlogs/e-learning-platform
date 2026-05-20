export interface User {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  gpa: number;
  semester: number;
  studyStreak: number;
}

export interface Subject {
  id: string;
  name: string;
  code: string;
  color: string;
  icon: string;
  mastery: number;
  materialsCount: number;
  quizzesCount: number;
  lastStudied: string;
}

export interface TopicMastery {
  topic: string;
  confidence: number;
  lastTested: string;
  questionsAttempted: number;
  correctRate: number;
}

export interface Quiz {
  id: string;
  subjectId: string;
  title: string;
  score: number;
  totalQuestions: number;
  date: string;
  duration: number;
  topics: string[];
}

export interface QuizQuestion {
  id: string;
  question: string;
  options: string[];
  correctIndex: number;
  explanation: string;
  topic: string;
  difficulty: "easy" | "medium" | "hard";
}

export interface Material {
  id: string;
  subjectId: string;
  name: string;
  type: "pdf" | "doc" | "video" | "link";
  size: string;
  uploadedAt: string;
  status: "processing" | "ready" | "failed";
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  timestamp: string;
  sources?: string[];
}

export interface ActivityData {
  day: string;
  minutes: number;
}

export interface GPATrend {
  month: string;
  gpa: number;
}

export interface WeakTopic {
  topic: string;
  subject: string;
  confidence: number;
  recommended: string;
}

export interface Task {
  id: string;
  title: string;
  subject: string;
  dueDate: string;
  priority: "low" | "medium" | "high";
  completed: boolean;
}
