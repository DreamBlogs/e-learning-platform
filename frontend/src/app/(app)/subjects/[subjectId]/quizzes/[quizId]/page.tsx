'use client';

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import {
  CheckCircle2,
  XCircle,
  ChevronRight,
  ChevronLeft,
  Sparkles,
  RotateCcw,
  Trophy,
  Clock,
  Loader2,
} from 'lucide-react';
import { useRequireAuth } from '@/hooks/useRequireAuth';
import { api } from '@/lib/api/client';
import { cn } from '@/lib/utils';

interface QuizPageProps {
  params: Promise<{ subjectId: string; quizId: string }>;
}

export default function QuizPage({ params }: QuizPageProps) {
  const [resolvedParams, setResolvedParams] = useState<{ subjectId: string; quizId: string } | null>(null);
  const { user } = useRequireAuth();
  const [questions, setQuestions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);
  const [showResult, setShowResult] = useState(false);
  const [answers, setAnswers] = useState<(number | null)[]>([]);
  const [quizComplete, setQuizComplete] = useState(false);
  const [score, setScore] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    params.then((p) => setResolvedParams(p));
  }, [params]);

  useEffect(() => {
    if (!resolvedParams?.quizId || !user) return;
    api.get<any[]>(`/quizzes/${resolvedParams.quizId}/questions`)
      .then(setQuestions)
      .finally(() => setLoading(false));
  }, [resolvedParams?.quizId, user]);

  if (loading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (questions.length === 0) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <Card className="text-center">
          <CardContent className="py-8">
            <Trophy className="mx-auto mb-4 h-12 w-12 text-muted-foreground/50" />
            <p className="text-sm text-muted-foreground">No questions available for this quiz.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const question = questions[currentQuestion];
  const progress = ((currentQuestion + 1) / questions.length) * 100;

  const handleSelect = (index: number) => {
    if (showResult) return;
    setSelectedAnswer(index);
  };

  const handleCheck = () => {
    if (selectedAnswer === null) return;
    setShowResult(true);
    setAnswers((prev) => [...prev, selectedAnswer]);
  };

  const handleNext = () => {
    if (currentQuestion < questions.length - 1) {
      setCurrentQuestion((prev) => prev + 1);
      setSelectedAnswer(null);
      setShowResult(false);
    } else {
      handleSubmit();
    }
  };

  const handleSubmit = async () => {
    setSubmitting(true);
    const optionMap = ['A', 'B', 'C', 'D'];
    const submission = {
      quizId: resolvedParams?.quizId,
      answers: answers.map((ans, i) => ({
        questionId: questions[i].id,
        selectedOption: ans !== null ? optionMap[ans] : 'A',
      })),
    };

    try {
      const result = await api.post<any>('/quizzes/attempts', submission);
      setScore(result.score);
      setQuizComplete(true);
    } catch (err) {
      console.error('Failed to submit quiz:', err);
      setQuizComplete(true);
    } finally {
      setSubmitting(false);
    }
  };

  const handlePrev = () => {
    if (currentQuestion > 0) {
      setCurrentQuestion((prev) => prev - 1);
      setSelectedAnswer(answers[currentQuestion - 1] ?? null);
      setShowResult(true);
    }
  };

  const handleRestart = () => {
    setCurrentQuestion(0);
    setSelectedAnswer(null);
    setShowResult(false);
    setAnswers([]);
    setQuizComplete(false);
    setScore(0);
  };

  if (quizComplete) {
    const percentage = Math.round((score / questions.length) * 100);
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="w-full max-w-md"
        >
          <Card className="text-center">
            <CardHeader>
              <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-primary/10">
                <Trophy className="h-8 w-8 text-primary" />
              </div>
              <CardTitle className="text-2xl">Quiz Complete!</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div>
                <div className="text-5xl font-bold">{percentage}%</div>
                <p className="mt-1 text-muted-foreground">
                  {score} out of {questions.length} correct
                </p>
              </div>
              <Progress value={percentage} className="h-3" />
              <div className="flex justify-center gap-3">
                <Button variant="outline" onClick={handleRestart} className="gap-2">
                  <RotateCcw className="h-4 w-4" />
                  Retry Quiz
                </Button>
              </div>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Mini Quiz</h1>
          <p className="text-muted-foreground">
            Question {currentQuestion + 1} of {questions.length}
          </p>
        </div>
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Clock className="h-4 w-4" />
          ~5 min
        </div>
      </div>

      <Progress value={progress} className="h-2" />

      <AnimatePresence mode="wait">
        <motion.div
          key={currentQuestion}
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -20 }}
          transition={{ duration: 0.2 }}
        >
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <span
                  className={cn(
                    'rounded-full px-2 py-0.5 text-xs font-medium',
                    question.difficulty === 'easy'
                      ? 'bg-emerald-500/10 text-emerald-500'
                      : question.difficulty === 'medium'
                      ? 'bg-amber-500/10 text-amber-500'
                      : 'bg-red-500/10 text-red-500'
                  )}
                >
                  {question.difficulty}
                </span>
                <span className="text-xs text-muted-foreground">{question.topic}</span>
              </div>
              <CardTitle className="mt-2 text-lg">{question.questionText}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {[question.optionA, question.optionB, question.optionC, question.optionD].map(
                (option, index) => {
                  const isSelected = selectedAnswer === index;
                  const correctOption = ['A', 'B', 'C', 'D'].indexOf(question.correctOption);
                  const isCorrect = index === correctOption;
                  const showFeedback = showResult;

                  return (
                    <button
                      key={index}
                      onClick={() => handleSelect(index)}
                      disabled={showResult}
                      className={cn(
                        'w-full rounded-lg border p-4 text-left text-sm transition-all',
                        showFeedback
                          ? isCorrect
                            ? 'border-emerald-500 bg-emerald-500/10'
                            : isSelected
                            ? 'border-red-500 bg-red-500/10'
                            : 'border-border opacity-50'
                          : isSelected
                          ? 'border-primary bg-primary/10'
                          : 'border-border hover:bg-accent/50'
                      )}
                    >
                      <div className="flex items-center gap-3">
                        <span
                          className={cn(
                            'flex h-6 w-6 items-center justify-center rounded-full border text-xs font-medium',
                            showFeedback && isCorrect
                              ? 'border-emerald-500 bg-emerald-500 text-white'
                              : showFeedback && isSelected && !isCorrect
                              ? 'border-red-500 bg-red-500 text-white'
                              : isSelected
                              ? 'border-primary bg-primary text-white'
                              : 'border-muted-foreground/30'
                          )}
                        >
                          {showFeedback && isCorrect ? (
                            <CheckCircle2 className="h-4 w-4" />
                          ) : showFeedback && isSelected && !isCorrect ? (
                            <XCircle className="h-4 w-4" />
                          ) : (
                            String.fromCharCode(65 + index)
                          )}
                        </span>
                        <span>{option}</span>
                      </div>
                    </button>
                  );
                }
              )}

              {showResult && question.explanation && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="mt-4 rounded-lg bg-muted p-4"
                >
                  <div className="flex items-start gap-2">
                    <Sparkles className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
                    <div>
                      <p className="text-sm font-medium">Explanation</p>
                      <p className="text-sm text-muted-foreground">{question.explanation}</p>
                    </div>
                  </div>
                </motion.div>
              )}
            </CardContent>
          </Card>
        </motion.div>
      </AnimatePresence>

      <div className="flex items-center justify-between">
        <Button
          variant="outline"
          onClick={handlePrev}
          disabled={currentQuestion === 0}
          className="gap-2"
        >
          <ChevronLeft className="h-4 w-4" />
          Previous
        </Button>

        {!showResult ? (
          <Button onClick={handleCheck} disabled={selectedAnswer === null} className="gap-2">
            Check Answer
          </Button>
        ) : (
          <Button onClick={handleNext} disabled={submitting} className="gap-2">
            {submitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Submitting...
              </>
            ) : currentQuestion < questions.length - 1 ? (
              <>
                Next
                <ChevronRight className="h-4 w-4" />
              </>
            ) : (
              <>
                Finish Quiz
                <Trophy className="h-4 w-4" />
              </>
            )}
          </Button>
        )}
      </div>
    </div>
  );
}
