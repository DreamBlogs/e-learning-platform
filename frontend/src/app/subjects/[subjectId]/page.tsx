"use client";

import { useEffect, useState, useRef } from "react";
import { useParams } from "next/navigation";
import { fetchApi } from "@/lib/api";
import { Send, UploadCloud, FileText, ArrowLeft, Loader2 } from "lucide-react";
import Link from "next/link";

export default function SubjectPage() {
  const { subjectId } = useParams();
  const [subject, setSubject] = useState<any>(null);
  const [materials, setMaterials] = useState<any[]>([]);
  const [messages, setMessages] = useState<{role: string, content: string}[]>([]);
  const [inputMessage, setInputMessage] = useState("");
  const [activeTab, setActiveTab] = useState<"chat" | "quizzes">("chat");
  const [quizzes, setQuizzes] = useState<any[]>([]);
  const [knowledge, setKnowledge] = useState<any[]>([]);
  const [isGeneratingQuiz, setIsGeneratingQuiz] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [isChatting, setIsChatting] = useState(false);

  const scrollRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    loadSubjectData();
    // Simulate auto-refresh for material statuses (simple polling)
    const interval = setInterval(loadSubjectData, 10000);
    return () => clearInterval(interval);
  }, [subjectId]);
  
  useEffect(() => {
    if (scrollRef.current && activeTab === "chat") {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, activeTab]);

  const loadSubjectData = async () => {
    try {
      const sub = await fetchApi(`/subjects/${subjectId}`);
      setSubject(sub);
      const mats = await fetchApi(`/subjects/${subjectId}/materials`);
      setMaterials(mats?.content || mats || []);
      
      const knowledgeStates = await fetchApi(`/subjects/${subjectId}/knowledge`);
      setKnowledge(knowledgeStates || []);
      
      // Mock loading quizzes
      setQuizzes([{ id: "demo-quiz", title: "Assessment: Core Knowledge", status: "READY", score: null }]);
    } catch (err) {
      console.error(err);
    }
  };

  const handleGenerateQuiz = async () => {
    setIsGeneratingQuiz(true);
    // In final backend step 2, this will hit /api/subjects/{id}/assessments/generate
    setTimeout(() => {
      setQuizzes([{ id: "mock-new-quiz", title: "New Material Assessment", status: "READY", score: null }, ...quizzes]);
      setIsGeneratingQuiz(false);
    }, 2000);
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    const formData = new FormData();
    formData.append("file", file);

    try {
      await fetchApi(`/subjects/${subjectId}/materials/upload`, {
        method: "POST",
        body: formData,
        // Don't set content-type for formData, browser sets it with boundaries
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
      });
      loadSubjectData();
    } catch (err) {
      alert("Failed to upload file");
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputMessage.trim() || isChatting) return;

    const userMessage = inputMessage;
    setMessages(prev => [...prev, { role: "user", content: userMessage }]);
    setInputMessage("");
    setIsChatting(true);

    try {
      const res = await fetchApi(`/subjects/${subjectId}/chat`, {
        method: "POST",
        body: JSON.stringify({ content: userMessage })
      });
      
      setMessages(prev => [...prev, { role: "ai", content: res.content }]);
    } catch (err) {
      setMessages(prev => [...prev, { role: "ai", content: "Error: Failed to fetch response from AI." }]);
    } finally {
      setIsChatting(false);
    }
  };

  if (!subject) return <div className="text-center py-20 text-gray-500 flex justify-center items-center h-screen"><Loader2 className="animate-spin w-8 h-8"/></div>;

  return (
    <div className="flex h-screen bg-gray-50 text-gray-900 font-sans">
      {/* Sidebar: Materials & Analytics placeholders */}
      <div className="w-1/3 border-r bg-white p-6 flex flex-col shadow-[4px_0_24px_rgba(0,0,0,0.02)] z-10">
        <Link href="/dashboard" className="text-gray-500 hover:text-blue-600 flex items-center mb-6 text-sm font-medium transition-colors">
          <ArrowLeft className="w-4 h-4 mr-1" /> Back to Dashboard
        </Link>
        <h2 className="text-2xl font-extrabold mb-1">{subject.name}</h2>
        <p className="text-sm text-gray-500 mb-8">{subject.description}</p>
        
        <div className="flex-1 overflow-y-auto pr-2 custom-scrollbar">
          <div className="mb-8">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-gray-800 flex items-center">
                <FileText className="w-4 h-4 mr-2" /> Materials
              </h3>
            </div>
            
            <div 
              onClick={() => fileInputRef.current?.click()}
              className={`border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all ${isUploading ? 'bg-gray-50 border-gray-300' : 'hover:bg-blue-50 hover:border-blue-300 bg-white'}`}
            >
              {isUploading ? (
                <Loader2 className="w-6 h-6 animate-spin mx-auto text-blue-500" />
              ) : (
                <UploadCloud className="w-6 h-6 mx-auto text-blue-500 mb-2" />
              )}
              <span className="text-sm font-medium text-gray-600">
                {isUploading ? "Uploading..." : "Upload PDF Material"}
              </span>
              <input type="file" ref={fileInputRef} className="hidden" accept=".pdf" onChange={handleFileUpload} />
            </div>

            <div className="mt-4 space-y-3">
              {materials.map(m => (
                <div key={m.id} className="p-3 bg-gray-50 rounded-lg border text-sm flex justify-between items-center group relative overflow-hidden">
                  <div className="absolute left-0 top-0 bottom-0 w-1 bg-blue-500"></div>
                  <span className="truncate font-medium pr-2 max-w-[200px]">{m.fileName}</span>
                  <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                    m.status === 'PROCESSED' ? 'bg-green-100 text-green-700' : 
                    m.status === 'FAILED' ? 'bg-red-100 text-red-700' : 
                    'bg-yellow-100 text-yellow-700'
                  }`}>
                    {m.status}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-gradient-to-br from-indigo-50 to-blue-50 p-5 rounded-xl border border-blue-100 shadow-inner mt-6">
            <h3 className="font-bold text-indigo-900 mb-2 text-sm uppercase tracking-wider">Weak Topics Analytics</h3>
            <p className="text-sm text-indigo-700/80 mb-3">Your knowledge confidence tracked via quizzes.</p>
            <div className="space-y-3">
              {knowledge.length === 0 ? (
                <div className="text-xs text-indigo-500 italic">No topics tracked yet. Upload a PDF and generate a quiz!</div>
              ) : (
                knowledge.map((k: any, i: number) => {
                  const conf = Math.round(k.confidenceScore * 100);
                  const colorClass = conf < 50 ? 'bg-red-400' : conf < 80 ? 'bg-yellow-400' : 'bg-green-500';
                  return (
                    <div key={i}>
                      <div className="text-xs font-medium text-indigo-800 flex justify-between mb-1">
                        <span className="truncate pr-2">{k.topicName || "Topic " + (i+1)}</span> 
                        <span className="font-bold">{conf}%</span>
                      </div>
                      <div className="w-full bg-indigo-200 rounded-full h-2">
                        <div className={`${colorClass} h-2 rounded-full`} style={{ width: `${conf}%` }}></div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Main Area */}
      <div className="flex-1 flex flex-col bg-[#F9FAFB]">
        <div className="bg-white border-b px-8 flex justify-between items-center shadow-sm z-0">
          <div className="py-5">
            <h2 className="font-bold text-gray-800">Learning Assistant & Quizzes</h2>
            <p className="text-xs text-gray-500">Chat with documents or test your knowledge</p>
          </div>
          <div className="flex space-x-6 text-sm font-medium">
            <button 
              onClick={() => setActiveTab("chat")} 
              className={`pb-4 pt-5 border-b-2 transition-colors ${activeTab === "chat" ? "border-blue-600 text-blue-600" : "border-transparent text-gray-500 hover:text-gray-800"}`}
            >
              Document Chat
            </button>
            <button 
              onClick={() => setActiveTab("quizzes")} 
              className={`pb-4 pt-5 border-b-2 transition-colors ${activeTab === "quizzes" ? "border-blue-600 text-blue-600" : "border-transparent text-gray-500 hover:text-gray-800"}`}
            >
              Assessments
            </button>
          </div>
        </div>
        
        {activeTab === "chat" ? (
          <>
            <div className="flex-1 overflow-y-auto p-8" ref={scrollRef}>
              {messages.length === 0 ? (
                <div className="flex items-center justify-center h-full text-center text-gray-400">
                  <div>
                    <FileText className="w-12 h-12 mx-auto mb-4 opacity-20" />
                    <p>Upload a PDF and start asking questions.</p>
                    <p className="text-sm mt-2">The AI will use semantic search to ground its answers.</p>
                  </div>
                </div>
              ) : (
                <div className="space-y-6 max-w-3xl mx-auto">
                  {messages.map((msg, i) => (
                    <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                      <div className={`max-w-[80%] rounded-2xl p-5 shadow-sm ${msg.role === 'user' ? 'bg-blue-600 text-white rounded-br-none' : 'bg-white border text-gray-800 rounded-bl-none'}`}>
                        <p className="whitespace-pre-wrap leading-relaxed">{msg.content}</p>
                      </div>
                    </div>
                  ))}
                  {isChatting && (
                    <div className="flex justify-start">
                      <div className="bg-white border rounded-2xl rounded-bl-none p-5 shadow-sm text-gray-500 flex items-center space-x-2">
                        <Loader2 className="w-4 h-4 animate-spin text-blue-500" />
                        <span className="text-sm font-medium">Analyzing materials...</span>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="p-6 bg-white border-t">
              <form onSubmit={handleSendMessage} className="max-w-3xl mx-auto relative group">
                <input
                  type="text"
                  className="w-full px-6 py-4 pr-16 border rounded-full bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all shadow-sm group-hover:shadow"
                  placeholder="Ask a question about your subject..."
                  value={inputMessage}
                  onChange={(e) => setInputMessage(e.target.value)}
                  disabled={isChatting}
                />
                <button 
                  type="submit" 
                  disabled={isChatting || !inputMessage.trim()}
                  className="absolute right-2 top-2 bottom-2 aspect-square flex items-center justify-center bg-blue-600 text-white rounded-full hover:bg-blue-700 disabled:opacity-50 transition-colors shadow-md"
                >
                  <Send className="w-4 h-4" />
                </button>
              </form>
            </div>
          </>
        ) : (
          <div className="flex-1 overflow-y-auto p-8 bg-gray-50">
            <div className="max-w-3xl mx-auto">
              <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white shadow-md flex justify-between items-center mb-8">
                <div>
                  <h2 className="text-2xl font-bold mb-2">Test Your Knowledge</h2>
                  <p className="opacity-90">AI generates targeted quizzes based on your uploaded notes and weak topics.</p>
                </div>
                <button 
                  onClick={handleGenerateQuiz}
                  disabled={isGeneratingQuiz}
                  className="bg-white text-blue-600 font-bold px-6 py-3 rounded-xl shadow-sm hover:scale-105 transition-transform flex items-center disabled:opacity-50"
                >
                  {isGeneratingQuiz ? <Loader2 className="w-5 h-5 animate-spin mr-2"/> : null}
                  {isGeneratingQuiz ? "Generating..." : "+ Generate Quiz"}
                </button>
              </div>

              <div className="space-y-4">
                <h3 className="font-bold text-gray-800 text-lg mb-4">Your Assessments</h3>
                {quizzes.length === 0 ? (
                   <p className="text-gray-500 text-center py-10 bg-white border border-dashed rounded-xl">No quizzes found. Generate one!</p>
                ) : quizzes.map((q, i) => (
                  <div key={i} className="bg-white border rounded-xl p-6 flex justify-between items-center hover:shadow-sm transition-shadow">
                    <div>
                      <h4 className="font-bold text-gray-900">{q.title}</h4>
                      <p className="text-sm text-gray-500 mt-1">Status: {q.status}</p>
                    </div>
                    <Link href={`/subjects/${subjectId}/quizzes/${q.id}`} className="px-5 py-2 bg-gray-100 font-medium text-gray-800 rounded-lg hover:bg-gray-200">
                      Start Test
                    </Link>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
      <style dangerouslySetInnerHTML={{__html: `
        .custom-scrollbar::-webkit-scrollbar { width: 4px; }
        .custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #E5E7EB; border-radius: 4px; }
      `}} />
    </div>
  );
}
