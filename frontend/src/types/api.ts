export interface User {
  id: string;
  email: string;
  displayName: string;
}

export interface AuthResponse {
  userId: string;
  email: string;
  displayName: string;
  accessToken: string;
}

export interface Subject {
  id: string;
  name: string;
  description?: string;
  color: string;
  createdAt: string;
}

export interface Material {
  id: string;
  subjectId: string;
  fileName: string;
  fileType: string;
  sizeBytes: number;
  status: 'UPLOADED' | 'PROCESSING' | 'TEXT_EXTRACTED' | 'PROCESSED' | 'FAILED';
  createdAt: string;
}

export interface Quiz {
  id: string;
  subjectId: string;
  title: string;
  description?: string;
  timeLimitMinutes?: number;
  questionCount: number;
  createdAt: string;
}

export interface QuizQuestion {
  id: string;
  quizId: string;
  questionText: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctOption: string;
  explanation?: string;
  topic?: string;
  difficulty: 'easy' | 'medium' | 'hard';
  position: number;
}

export interface QuizAttempt {
  id: string;
  quizId: string;
  score: number;
  totalQuestions: number;
  startedAt: string;
  completedAt?: string;
}

export interface TopicMastery {
  id: string;
  subjectId: string;
  topic: string;
  confidence: number;
  questionsAttempted: number;
  correctAnswers: number;
  lastTested?: string;
}

export interface Task {
  id: string;
  subjectId?: string;
  title: string;
  description?: string;
  dueDate?: string;
  priority: 'low' | 'medium' | 'high';
  completed: boolean;
  createdAt: string;
}

export interface ChatMessage {
  message: string;
  sources: string[];
}

export interface AnalyticsOverview {
  avgConfidence: number;
  topicsMastered: number;
  totalTopics: number;
  weakestArea?: TopicMastery;
  studyStreak: number;
  quizzesCompleted: number;
}

export interface SubjectAnalytics {
  id: string;
  name: string;
  code: string;
  color: string;
  avgConfidence: number;
  topicCount: number;
  quizCount: number;
}
