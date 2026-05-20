"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { fetchApi } from "@/lib/api";
import { CheckCircle2, Circle, ArrowLeft, BrainCircuit } from "lucide-react";
import Link from "next/link";

export default function QuizTakingPage() {
  const { subjectId, quizId } = useParams();
  const [quiz, setQuiz] = useState<any>(null);
  const [currentQ, setCurrentQ] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [results, setResults] = useState<any>(null);

  useEffect(() => {
    // Stubbing quiz load for frontend-first MVP
    if (quizId) {
      setQuiz({
        id: quizId,
        title: "Test generated from materials",
        questions: [
          { id: "q1", type: "MULTIPLE_CHOICE", prompt: "What is Dependency Injection?", options: ["A pattern for passing dependencies", "A database", "A CSS framework"], topic: "Dependency Injection" },
          { id: "q2", type: "MULTIPLE_CHOICE", prompt: "Which annotation registers a bean in Spring?", options: ["@Bean", "@Entity", "@Table"], topic: "Spring Core" }
        ]
      });
    }
  }, [quizId]);

  const selectAnswer = (qId: string, ans: string) => {
    if (!isSubmitted) setAnswers({ ...answers, [qId]: ans });
  };

  const submitQuiz = () => {
    setIsSubmitted(true);
    // Mock result scoring
    const mockResults = quiz.questions.map((q: any) => ({
      questionId: q.id,
      prompt: q.prompt,
      correct: answers[q.id] === q.options[0], // MVP hack, assume option 0 is correct
      userAnswer: answers[q.id],
      topic: q.topic
    }));
    setResults(mockResults);
    
    // Simulate backend submission payload: array of { questionId, userAnswer }
  };

  if (!quiz) return <div className="p-10 text-center">Loading quiz state...</div>;

  const currentQuestion = quiz.questions[currentQ];

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900 font-sans">
      <div className="max-w-3xl mx-auto pt-10 px-6">
        <Link href={`/subjects/${subjectId}`} className="text-gray-500 hover:text-blue-600 flex items-center mb-6 text-sm font-medium">
          <ArrowLeft className="w-4 h-4 mr-1" /> Back to Subject
        </Link>
        
        {!isSubmitted ? (
          <>
            <div className="flex justify-between items-center mb-8">
              <h1 className="text-2xl font-bold flex items-center"><BrainCircuit className="mr-2 text-blue-600"/> {quiz.title}</h1>
              <div className="text-sm font-medium text-gray-500">
                Question {currentQ + 1} of {quiz.questions.length}
              </div>
            </div>

            <div className="bg-white p-8 rounded-2xl shadow-sm border mb-6">
              <h2 className="text-xl font-medium mb-6">{currentQuestion.prompt}</h2>
              <div className="space-y-3">
                {currentQuestion.options.map((opt: string, i: number) => {
                  const isSelected = answers[currentQuestion.id] === opt;
                  return (
                    <div 
                      key={i} 
                      onClick={() => selectAnswer(currentQuestion.id, opt)}
                      className={`p-4 border rounded-xl cursor-pointer flex items-center transition-all ${isSelected ? 'border-blue-500 bg-blue-50/50 text-blue-900 font-medium shadow-sm' : 'hover:bg-gray-50'}`}
                    >
                      {isSelected ? <CheckCircle2 className="w-5 h-5 mr-3 text-blue-500" /> : <Circle className="w-5 h-5 mr-3 text-gray-300" />}
                      {opt}
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="flex justify-between">
              <button 
                onClick={() => setCurrentQ(Math.max(0, currentQ - 1))}
                disabled={currentQ === 0}
                className="px-6 py-2 text-gray-600 disabled:opacity-50"
              >
                Previous
              </button>
              {currentQ === quiz.questions.length - 1 ? (
                <button 
                  onClick={submitQuiz}
                  disabled={Object.keys(answers).length < quiz.questions.length}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                >
                  Submit Answers
                </button>
              ) : (
                <button 
                  onClick={() => setCurrentQ(Math.min(quiz.questions.length - 1, currentQ + 1))}
                  className="px-6 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800"
                >
                  Next Question
                </button>
              )}
            </div>
          </>
        ) : (
          <div className="bg-white p-8 rounded-2xl shadow-sm border">
            <h2 className="text-2xl font-bold mb-2 text-center">Quiz Completed!</h2>
            <p className="text-gray-500 text-center mb-8">Here are your immediate knowledge state updates.</p>
            
            <div className="space-y-4">
              {results.map((res: any, i: number) => (
                <div key={i} className={`p-4 rounded-xl border ${res.correct ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
                  <h3 className="font-medium text-gray-800 mb-2">{res.prompt}</h3>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-gray-600">Your answer: <b>{res.userAnswer}</b></span>
                    <span className={`font-bold flex items-center ${res.correct ? 'text-green-600' : 'text-red-600'}`}>
                      {res.correct ? '+0.2 Confidence' : '-0.15 Confidence'}
                    </span>
                  </div>
                  <div className="mt-2 text-xs font-medium text-gray-400 capitalize">Topic: {res.topic}</div>
                </div>
              ))}
            </div>
            
            <div className="mt-8 text-center">
               <Link href={`/subjects/${subjectId}`} className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 inline-block font-medium">
                  Return to Dashboard
                </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
