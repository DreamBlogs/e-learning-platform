'use client';

import { useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useSubjects } from '@/hooks/useSubjects';
import { useAuth } from '@/hooks/useAuth';
import { cn } from '@/lib/utils';
import {
  LayoutDashboard,
  BarChart3,
  BookOpen,
  GraduationCap,
  Sun,
  Moon,
  LogOut,
  ChevronLeft,
  Loader2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useTheme } from '@/components/theme-provider';

export function AppSidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuth();
  const { subjects, loading } = useSubjects();
  const { theme, toggleTheme } = useTheme();
  const [collapsed, setCollapsed] = useState(false);

  const navItems = [
    { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { href: '/analytics', label: 'Analytics', icon: BarChart3 },
  ];

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-40 h-screen border-r bg-background transition-all duration-300',
        collapsed ? 'w-16' : 'w-64'
      )}
    >
      <div className="flex h-14 items-center justify-between border-b px-4">
        {!collapsed && (
          <span className="flex items-center gap-2 text-lg font-semibold">
            <GraduationCap className="h-6 w-6 text-primary" />
            LearnAI
          </span>
        )}
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setCollapsed(!collapsed)}
          className="h-8 w-8"
        >
          <ChevronLeft className={cn('h-4 w-4 transition-transform', collapsed && 'rotate-180')} />
        </Button>
      </div>

      <ScrollArea className="h-[calc(100vh-140px)]">
        <nav className="space-y-1 p-2">
          {navItems.map((item) => (
            <button
              key={item.href}
              onClick={() => router.push(item.href)}
              className={cn(
                'flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                pathname === item.href
                  ? 'bg-primary/10 text-primary'
                  : 'text-muted-foreground hover:bg-accent'
              )}
            >
              <item.icon className="h-5 w-5 shrink-0" />
              {!collapsed && <span>{item.label}</span>}
            </button>
          ))}

          {!collapsed && (
            <div className="pt-4">
              <p className="px-3 text-xs font-medium text-muted-foreground">SUBJECTS</p>
            </div>
          )}

          {loading ? (
            <div className="flex justify-center py-4">
              <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
            </div>
          ) : (
            subjects.map((subject) => (
              <button
                key={subject.id}
                onClick={() => router.push(`/subjects/${subject.id}`)}
                className={cn(
                  'flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                  pathname.includes(subject.id)
                    ? 'bg-primary/10 text-primary'
                    : 'text-muted-foreground hover:bg-accent'
                )}
              >
                <div
                  className="h-2 w-2 shrink-0 rounded-full"
                  style={{ backgroundColor: subject.color }}
                />
                {!collapsed && (
                  <>
                    <span className="flex-1 truncate text-left">{subject.name}</span>
                  </>
                )}
              </button>
            ))
          )}
        </nav>
      </ScrollArea>

      <div className="absolute bottom-0 left-0 right-0 border-t p-2">
        <button
          onClick={toggleTheme}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-muted-foreground hover:bg-accent"
        >
          {theme === 'dark' ? (
            <Sun className="h-5 w-5 shrink-0" />
          ) : (
            <Moon className="h-5 w-5 shrink-0" />
          )}
          {!collapsed && <span>{theme === 'dark' ? 'Light Mode' : 'Dark Mode'}</span>}
        </button>
        <button
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-muted-foreground hover:bg-accent"
        >
          <LogOut className="h-5 w-5 shrink-0" />
          {!collapsed && <span>Sign Out</span>}
        </button>
      </div>
    </aside>
  );
}
