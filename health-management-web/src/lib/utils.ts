import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(dateStr: string, locale: string = 'zh-CN'): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString(locale, { year: 'numeric', month: '2-digit', day: '2-digit' });
}

export function formatTime(dateStr: string, locale: string = 'zh-CN'): string {
  const d = new Date(dateStr);
  return d.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });
}

export function formatDateTime(dateStr: string, locale: string = 'zh-CN'): string {
  const d = new Date(dateStr);
  return d.toLocaleString(locale, { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}
