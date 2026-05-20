import type {
  User,
  Subject,
  TopicMastery,
  Quiz,
  QuizQuestion,
  Material,
  ChatMessage,
  ActivityData,
  GPATrend,
  WeakTopic,
  Task,
} from "@/types";

export const mockUser: User = {
  id: "1",
  name: "Alex Student",
  email: "alex@university.edu",
  gpa: 3.72,
  semester: 4,
  studyStreak: 12,
};

export const mockSubjects: Subject[] = [
  { id: "1", name: "Data Structures & Algorithms", code: "CS201", color: "#3b82f6", icon: "Code", mastery: 78, materialsCount: 14, quizzesCount: 8, lastStudied: "2h ago" },
  { id: "2", name: "Linear Algebra", code: "MATH301", color: "#8b5cf6", icon: "FunctionSquare", mastery: 65, materialsCount: 9, quizzesCount: 5, lastStudied: "1d ago" },
  { id: "3", name: "Physics II", code: "PHYS202", color: "#f59e0b", icon: "Atom", mastery: 82, materialsCount: 11, quizzesCount: 6, lastStudied: "3h ago" },
  { id: "4", name: "Database Systems", code: "CS305", color: "#10b981", icon: "Database", mastery: 91, materialsCount: 7, quizzesCount: 4, lastStudied: "5h ago" },
  { id: "5", name: "Probability & Statistics", code: "STAT201", color: "#ef4444", icon: "BarChart3", mastery: 54, materialsCount: 12, quizzesCount: 7, lastStudied: "2d ago" },
];

export const mockTopicMastery: Record<string, TopicMastery[]> = {
  "1": [
    { topic: "Arrays & Strings", confidence: 92, lastTested: "2025-01-15", questionsAttempted: 45, correctRate: 89 },
    { topic: "Linked Lists", confidence: 85, lastTested: "2025-01-14", questionsAttempted: 32, correctRate: 84 },
    { topic: "Trees & BST", confidence: 72, lastTested: "2025-01-13", questionsAttempted: 28, correctRate: 71 },
    { topic: "Graph Algorithms", confidence: 58, lastTested: "2025-01-12", questionsAttempted: 20, correctRate: 55 },
    { topic: "Dynamic Programming", confidence: 45, lastTested: "2025-01-11", questionsAttempted: 15, correctRate: 40 },
    { topic: "Sorting Algorithms", confidence: 88, lastTested: "2025-01-10", questionsAttempted: 38, correctRate: 87 },
  ],
  "2": [
    { topic: "Vector Spaces", confidence: 70, lastTested: "2025-01-14", questionsAttempted: 25, correctRate: 68 },
    { topic: "Matrix Operations", confidence: 78, lastTested: "2025-01-13", questionsAttempted: 30, correctRate: 76 },
    { topic: "Eigenvalues", confidence: 42, lastTested: "2025-01-12", questionsAttempted: 12, correctRate: 38 },
    { topic: "Linear Transformations", confidence: 65, lastTested: "2025-01-11", questionsAttempted: 22, correctRate: 62 },
    { topic: "Orthogonality", confidence: 55, lastTested: "2025-01-10", questionsAttempted: 18, correctRate: 52 },
  ],
  "3": [
    { topic: "Electromagnetism", confidence: 85, lastTested: "2025-01-15", questionsAttempted: 35, correctRate: 83 },
    { topic: "Optics", confidence: 78, lastTested: "2025-01-14", questionsAttempted: 28, correctRate: 76 },
    { topic: "Thermodynamics", confidence: 90, lastTested: "2025-01-13", questionsAttempted: 40, correctRate: 88 },
    { topic: "Waves & Oscillations", confidence: 72, lastTested: "2025-01-12", questionsAttempted: 22, correctRate: 70 },
  ],
  "4": [
    { topic: "SQL Queries", confidence: 95, lastTested: "2025-01-15", questionsAttempted: 50, correctRate: 94 },
    { topic: "Normalization", confidence: 88, lastTested: "2025-01-14", questionsAttempted: 25, correctRate: 86 },
    { topic: "Indexing", confidence: 92, lastTested: "2025-01-13", questionsAttempted: 20, correctRate: 90 },
    { topic: "Transactions", confidence: 85, lastTested: "2025-01-12", questionsAttempted: 18, correctRate: 83 },
  ],
  "5": [
    { topic: "Probability Distributions", confidence: 48, lastTested: "2025-01-14", questionsAttempted: 15, correctRate: 42 },
    { topic: "Hypothesis Testing", confidence: 55, lastTested: "2025-01-13", questionsAttempted: 20, correctRate: 50 },
    { topic: "Regression Analysis", confidence: 62, lastTested: "2025-01-12", questionsAttempted: 25, correctRate: 58 },
    { topic: "Bayesian Statistics", confidence: 38, lastTested: "2025-01-11", questionsAttempted: 10, correctRate: 35 },
    { topic: "Combinatorics", confidence: 60, lastTested: "2025-01-10", questionsAttempted: 22, correctRate: 56 },
  ],
};

export const mockQuizzes: Quiz[] = [
  { id: "q1", subjectId: "1", title: "Arrays & Strings Quiz", score: 92, totalQuestions: 12, date: "2025-01-15", duration: 15, topics: ["Arrays & Strings"] },
  { id: "q2", subjectId: "1", title: "Trees & BST Quiz", score: 75, totalQuestions: 10, date: "2025-01-13", duration: 20, topics: ["Trees & BST"] },
  { id: "q3", subjectId: "2", title: "Matrix Operations Quiz", score: 68, totalQuestions: 8, date: "2025-01-14", duration: 12, topics: ["Matrix Operations"] },
  { id: "q4", subjectId: "3", title: "Electromagnetism Quiz", score: 88, totalQuestions: 15, date: "2025-01-15", duration: 25, topics: ["Electromagnetism"] },
  { id: "q5", subjectId: "4", title: "SQL Advanced Quiz", score: 96, totalQuestions: 10, date: "2025-01-15", duration: 18, topics: ["SQL Queries"] },
  { id: "q6", subjectId: "5", title: "Probability Basics", score: 45, totalQuestions: 10, date: "2025-01-14", duration: 15, topics: ["Probability Distributions"] },
];

export const mockQuizQuestions: QuizQuestion[] = [
  {
    id: "qq1",
    question: "What is the time complexity of binary search on a sorted array?",
    options: ["O(n)", "O(log n)", "O(n log n)", "O(1)"],
    correctIndex: 1,
    explanation: "Binary search halves the search space with each comparison, resulting in O(log n) time complexity.",
    topic: "Arrays & Strings",
    difficulty: "easy",
  },
  {
    id: "qq2",
    question: "Which data structure is best for implementing a recursive algorithm iteratively?",
    options: ["Queue", "Stack", "Linked List", "Hash Table"],
    correctIndex: 1,
    explanation: "A stack naturally mimics the call stack used in recursion, making it ideal for iterative implementations.",
    topic: "Arrays & Strings",
    difficulty: "medium",
  },
  {
    id: "qq3",
    question: "In a BST, what is the inorder traversal result?",
    options: ["Random order", "Sorted order", "Reverse sorted", "Level order"],
    correctIndex: 1,
    explanation: "Inorder traversal of a BST visits nodes in ascending (sorted) order: left, root, right.",
    topic: "Trees & BST",
    difficulty: "easy",
  },
  {
    id: "qq4",
    question: "What is the determinant of a 2x2 identity matrix?",
    options: ["0", "1", "2", "-1"],
    correctIndex: 1,
    explanation: "The determinant of any identity matrix is always 1.",
    topic: "Matrix Operations",
    difficulty: "easy",
  },
  {
    id: "qq5",
    question: "Which law states that the induced EMF is proportional to the rate of change of magnetic flux?",
    options: ["Gauss's Law", "Faraday's Law", "Ampere's Law", "Coulomb's Law"],
    correctIndex: 1,
    explanation: "Faraday's Law of Electromagnetic Induction states that the induced EMF equals the negative rate of change of magnetic flux.",
    topic: "Electromagnetism",
    difficulty: "medium",
  },
];

export const mockMaterials: Material[] = [
  { id: "m1", subjectId: "1", name: "Lecture 1 - Introduction to DSA.pdf", type: "pdf", size: "2.4 MB", uploadedAt: "2025-01-10", status: "ready" },
  { id: "m2", subjectId: "1", name: "Arrays Deep Dive.pdf", type: "pdf", size: "1.8 MB", uploadedAt: "2025-01-11", status: "ready" },
  { id: "m3", subjectId: "1", name: "Tree Algorithms Video", type: "video", size: "156 MB", uploadedAt: "2025-01-12", status: "ready" },
  { id: "m4", subjectId: "2", name: "Linear Algebra Notes.pdf", type: "pdf", size: "3.1 MB", uploadedAt: "2025-01-09", status: "ready" },
  { id: "m5", subjectId: "3", name: "Physics Lab Manual.pdf", type: "pdf", size: "4.2 MB", uploadedAt: "2025-01-08", status: "ready" },
];

export const mockChatHistory: ChatMessage[] = [
  { id: "c1", role: "user", content: "Can you explain dynamic programming with an example?", timestamp: "2025-01-15T10:30:00Z" },
  { id: "c2", role: "assistant", content: "Dynamic Programming (DP) is a method for solving complex problems by breaking them into simpler subproblems. Let me use the Fibonacci sequence as an example:\n\n**Naive Recursive Approach:**\n```python\ndef fib(n):\n    if n <= 1: return n\n    return fib(n-1) + fib(n-2)\n```\nThis has O(2^n) complexity due to repeated calculations.\n\n**DP with Memoization:**\n```python\ndef fib_memo(n, memo={}):\n    if n in memo: return memo[n]\n    if n <= 1: return n\n    memo[n] = fib_memo(n-1, memo) + fib_memo(n-2, memo)\n    return memo[n]\n```\nThis reduces complexity to O(n) by storing results.", timestamp: "2025-01-15T10:30:15Z", sources: ["Lecture 1 - Introduction to DSA.pdf", "Arrays Deep Dive.pdf"] },
  { id: "c3", role: "user", content: "What's the difference between memoization and tabulation?", timestamp: "2025-01-15T10:32:00Z" },
  { id: "c4", role: "assistant", content: "Great question! Both are DP techniques but differ in approach:\n\n**Memoization (Top-Down):**\n- Start from the target problem\n- Recursively break down\n- Cache results as you go\n- Only computes needed subproblems\n\n**Tabulation (Bottom-Up):**\n- Start from base cases\n- Build up iteratively\n- Fill a table systematically\n- Computes all subproblems\n\nFor Fibonacci, tabulation looks like:\n```python\ndef fib_tab(n):\n    if n <= 1: return n\n    dp = [0] * (n + 1)\n    dp[1] = 1\n    for i in range(2, n + 1):\n        dp[i] = dp[i-1] + dp[i-2]\n    return dp[n]\n```", timestamp: "2025-01-15T10:32:20Z" },
];

export const mockActivityData: ActivityData[] = [
  { day: "Mon", minutes: 45 },
  { day: "Tue", minutes: 90 },
  { day: "Wed", minutes: 60 },
  { day: "Thu", minutes: 120 },
  { day: "Fri", minutes: 75 },
  { day: "Sat", minutes: 30 },
  { day: "Sun", minutes: 95 },
];

export const mockGPATrends: GPATrend[] = [
  { month: "Sep", gpa: 3.45 },
  { month: "Oct", gpa: 3.52 },
  { month: "Nov", gpa: 3.58 },
  { month: "Dec", gpa: 3.61 },
  { month: "Jan", gpa: 3.72 },
];

export const mockWeakTopics: WeakTopic[] = [
  { topic: "Dynamic Programming", subject: "DSA", confidence: 45, recommended: "Practice 10 more problems" },
  { topic: "Bayesian Statistics", subject: "Stats", confidence: 38, recommended: "Review lecture notes" },
  { topic: "Eigenvalues", subject: "Linear Algebra", confidence: 42, recommended: "Watch video tutorial" },
  { topic: "Probability Distributions", subject: "Stats", confidence: 48, recommended: "Take a mini quiz" },
];

export const mockTasks: Task[] = [
  { id: "t1", title: "Complete DP practice set", subject: "DSA", dueDate: "2025-01-18", priority: "high", completed: false },
  { id: "t2", title: "Review Eigenvalues chapter", subject: "Linear Algebra", dueDate: "2025-01-19", priority: "medium", completed: false },
  { id: "t3", title: "Submit Physics lab report", subject: "Physics II", dueDate: "2025-01-17", priority: "high", completed: false },
  { id: "t4", title: "Practice SQL joins", subject: "Database Systems", dueDate: "2025-01-20", priority: "low", completed: true },
  { id: "t5", title: "Study Bayesian inference", subject: "Statistics", dueDate: "2025-01-21", priority: "medium", completed: false },
];
