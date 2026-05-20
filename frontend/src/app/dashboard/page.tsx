"use client";

import { useEffect, useState } from "react";
import { fetchApi } from "@/lib/api";
import Link from "next/link";
import { PlusCircle, Book, LogOut } from "lucide-react";

export default function DashboardPage() {
  const [subjects, setSubjects] = useState<any[]>([]);
  const [newSubjectName, setNewSubjectName] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSubjects();
  }, []);

  const loadSubjects = async () => {
    try {
      const data = await fetchApi("/subjects");
      // Could be wrapped in a standard collection response { content: [] }
      setSubjects(data?.content || data || []);
    } catch (err) {
      console.error(err);
      if (err instanceof Error && err.message.includes("401")) {
        window.location.href = "/login";
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSubject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSubjectName.trim()) return;
    try {
      await fetchApi("/subjects", {
        method: "POST",
        body: JSON.stringify({ name: newSubjectName, description: "New Subject" })
      });
      setNewSubjectName("");
      loadSubjects();
    } catch (err) {
      console.error("Failed to create subject", err);
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    window.location.href = "/login";
  };

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900 font-sans">
      <nav className="bg-white border-b px-6 py-4 flex justify-between items-center shadow-sm">
        <h1 className="text-xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
          AI Learning MVP
        </h1>
        <button onClick={logout} className="flex items-center text-sm text-gray-600 hover:text-red-500 transition-colors">
          <LogOut className="w-4 h-4 mr-2" /> Logout
        </button>
      </nav>

      <main className="max-w-5xl mx-auto p-6 mt-8">
        <div className="mb-8 p-6 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-6 items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold mb-2">Welcome to your workspace</h2>
            <p className="text-gray-500">Create a subject, upload PDFs, and start chatting with your materials.</p>
          </div>
          <form onSubmit={handleCreateSubject} className="flex gap-2 w-full md:w-auto">
            <input
              type="text"
              placeholder="E.g. Biology 101"
              value={newSubjectName}
              onChange={(e) => setNewSubjectName(e.target.value)}
              className="px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 flex-1 md:w-64"
            />
            <button type="submit" className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
              <PlusCircle className="w-4 h-4 mr-2" />
              Create
            </button>
          </form>
        </div>

        <h3 className="text-xl font-semibold mb-6 flex items-center">
          <Book className="w-5 h-5 mr-2 text-blue-600" />
          Your Subjects
        </h3>
        
        {loading ? (
          <div className="text-gray-500 text-center py-12">Loading subjects...</div>
        ) : subjects.length === 0 ? (
          <div className="text-center py-12 border-2 border-dashed rounded-xl bg-white text-gray-500">
            No subjects yet. Create one to get started!
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {subjects.map((sub: any) => (
              <Link key={sub.id} href={`/subjects/${sub.id}`}>
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-200 transition-all cursor-pointer h-full group">
                  <h4 className="text-lg font-bold mb-2 group-hover:text-blue-600 transition-colors">{sub.name}</h4>
                  <p className="text-sm text-gray-500 line-clamp-2">{sub.description || 'No description'}</p>
                </div>
              </Link>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
