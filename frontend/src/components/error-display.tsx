'use client';

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { AlertCircle, X, Copy, ChevronDown, ChevronUp, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

interface ErrorDisplayProps {
  error: Error | string;
  onDismiss?: () => void;
  className?: string;
}

export function ErrorDisplay({ error, onDismiss, className }: ErrorDisplayProps) {
  const [expanded, setExpanded] = useState(false);
  const [copied, setCopied] = useState(false);

  const errorMessage = error instanceof Error ? error.message : error;
  const errorStack = error instanceof Error ? error.stack : undefined;

  const handleCopy = async () => {
    const text = errorStack || errorMessage;
    await navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -10 }}
      className={cn('rounded-lg border border-red-500/20 bg-red-500/5', className)}
    >
      <div
        className="flex items-start gap-3 p-4 cursor-pointer"
        onClick={() => setExpanded(!expanded)}
      >
        <AlertCircle className="h-5 w-5 shrink-0 text-red-500 mt-0.5" />
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-red-500">{errorMessage}</p>
          {errorStack && (
            <p className="text-xs text-muted-foreground mt-1">
              Click to {expanded ? 'collapse' : 'expand'} details
            </p>
          )}
        </div>
        <div className="flex items-center gap-1">
          <Button
            variant="ghost"
            size="icon"
            className="h-6 w-6"
            onClick={(e) => {
              e.stopPropagation();
              handleCopy();
            }}
          >
            {copied ? (
              <span className="text-xs text-emerald-500">Copied!</span>
            ) : (
              <Copy className="h-3 w-3" />
            )}
          </Button>
          {onDismiss && (
            <Button
              variant="ghost"
              size="icon"
              className="h-6 w-6"
              onClick={(e) => {
                e.stopPropagation();
                onDismiss();
              }}
            >
              <X className="h-3 w-3" />
            </Button>
          )}
          {expanded ? (
            <ChevronUp className="h-4 w-4 text-muted-foreground" />
          ) : (
            <ChevronDown className="h-4 w-4 text-muted-foreground" />
          )}
        </div>
      </div>

      <AnimatePresence>
        {expanded && errorStack && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="overflow-hidden"
          >
            <div className="border-t border-red-500/10 p-4">
              <pre className="text-xs text-muted-foreground whitespace-pre-wrap font-mono max-h-48 overflow-y-auto">
                {errorStack}
              </pre>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}
