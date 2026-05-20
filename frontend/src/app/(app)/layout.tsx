"use client";

import { AppSidebar } from "@/components/app-sidebar";
import { motion } from "framer-motion";

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen bg-background">
      <AppSidebar />
      <motion.main
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="ml-[260px] flex-1 p-6 lg:ml-[260px]"
      >
        {children}
      </motion.main>
    </div>
  );
}
