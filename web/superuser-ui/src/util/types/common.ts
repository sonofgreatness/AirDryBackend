// types/common.ts
export type UUID = string;

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
  totalCount: number;
  totalPages: number;
}

/**
 * WeeklyApprovalDTO equivalent
 */
export interface WeeklyApprovalDTO {
  id: UUID;
  organizationId: UUID;
  weekStartDate: string;
  weekEndDate: string;
  approvedById?: UUID | null;
  approvedByFullName?: string | null;
  approvedAt?: string | null;
  status: string;
  notes?: string | null;
}

/**
 * WorkSummaryDTO equivalent
 */
export interface WorkSummaryDTO {
  id: UUID;
  workDate: string;
  totalVolume: number;
  totalStacks: number;
  totalUsers: number;
  stacks: StackInfoDTO[];
  users: UserInfoDTO[];
  capturedAt?: string | null;
  updatedAt?: string | null;
  capturedBy?: string | null;
  updatedBy?: string | null;
}

/**
 * WorkDaySummaryDTO equivalent
 */
export interface WorkDaySummaryDTO {
  dayLabel: string;
  totalVolume: number;
  totalStacks: number;
  totalUsers: number;
  workList: WorkSummaryDTO[];
}

/**
 * StackInfoDTO equivalent
 */
export interface StackInfoDTO {
  id: UUID;
  displayName: string;
  stackCodeDisplayName: string;
  stackCode: string;
  volume: number;
}

/**
 * UserInfoDTO equivalent
 */
export interface UserInfoDTO {
  id: UUID;
  name: string;
  surname: string;
  phoneNumber: string;
  isActive: boolean;
  isApproved: boolean;
  personalCubes: number;
}



export interface StackEntityResponse {
  id?: UUID | null;
  displayCode: string;
  displayName: string;
}

export interface GetStackDTO {
  id?: UUID | null;
  length: number;       // Float -> number
  height: number;       // Float -> number
  thickness: number;    // Float -> number
  displayName: string;
  numberOfPieces: number;
  volume: number;       // Double -> number
  stackCode: StackEntityResponse;
}

export interface ReleaseDTO { 
     id : UUID,
     dateOfRelease : string,
     link : string,
  platform: string
}