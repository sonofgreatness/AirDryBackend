// src/util/types/user.ts
import { type UUID } from "./common"; // alias for string
// you can define: export type UUID = string in common.ts

// types/user.ts

/**
 * Base user info (common across system)
 */
export interface User {
  id: UUID;
  name: string;
  email: string;
}

/**
 * Extended profile for logged-in user
 */
export interface UserProfile extends User {
  phone?: string;
  role: "admin" | "manager" | "user"; // 🔄 adjust roles based on your system
  avatarUrl?: string; // for profile pic in header dropdown
}

/**
 * DTO for login response
 */
export interface LoginResponse {
  token: string;
  user: UserProfile;
}

/**
 * DTO for updating user profile
 */
export interface UpdateUserDto {
  name?: string;
  email?: string;
  phone?: string;
  avatarUrl?: string;
}





/**
 * DTO: Current logged-in user
 */
export interface CurrentUserDTO {
  id: UUID;
  name: string;
  surname: string;
  email?: string | null;
  phoneNumber: string;

  // UserType
  userTypeId?: UUID | null;
  userTypeName?: string | null;

  // Organization
  organizationId?: UUID | null;
  organizationName?: string | null;

  // Department
  departmentId?: UUID | null;
  departmentName?: string | null;

  // Section
  sectionId?: UUID | null;
  sectionName?: string | null;
}

/**
 * DTO: Update user payload
 * Matches UpdateUserDTO in Ktor
 */
export interface UpdateUserDTO {
  updated_name?: string | null;
  updated_surname?: string | null;
  updated_phoneNumber?: string | null;
  updated_secondaryPhoneNumber?: string | null;
  updated_userTypeId?: UUID | null;
  updated_departmentId?: UUID | null;
  updated_sectionId?: UUID | null;
  updated_email?: string | null;
  updated_password?: string | null;
  isActive?: boolean | null;
  isApproved?: boolean | null;
}

/**
 * DTO: Registration request
 */
export interface UserRegisterRequest {
  name: string;
  surname: string;
  phoneNumber: string;
  secondaryPhoneNumber?: string | null;
  userTypeId?: UUID | null;
  organizationId?: UUID | null;
  departmentId?: UUID | null;
  sectionId?: UUID | null;
  email?: string | null;
  password: string;
  isActive?: boolean;
  isApproved?: boolean;
  id?: UUID | null;
}

/**
 * DTO: Named entity (userType, org, dept, section)
 */
export interface NamedEntityResponse {
  id: UUID;
  displayName: string;
}

/**
 * DTO: User response for listing users
 */
export interface UserResponse {
  id: UUID;
  name: string;
  surname: string;
  phoneNumber: string;
  secondaryPhoneNumber?: string | null;
  email?: string | null;
  isActive: boolean;
  isApproved: boolean;

  userType?: NamedEntityResponse | null;
  organization?: NamedEntityResponse | null;
  department?: NamedEntityResponse | null;
  section?: NamedEntityResponse | null;
}

/**
 * DTO: User types
 */
export interface UserTypesDTO {
  id: UUID;
  displayName: string;
}

