export type UUID = string;
// types/common.ts

/**
 * Standard API response wrapper
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

/**
 * Error response format from backend
 */
export interface ApiError {
  error: string;
  details?: string[];
}

/**
 * Pagination wrapper for list endpoints
 */
export interface PaginatedResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalCount: number; // matches backend
  totalPages: number; // matches backend
}
