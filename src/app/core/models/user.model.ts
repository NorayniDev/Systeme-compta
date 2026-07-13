import { Permission, UserRole } from '../constants/roles.constant';

/**
 * Représentation front d'un utilisateur authentifié.
 */
export interface IUser {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  permissions: Permission[];
  avatarUrl?: string;
  isActive: boolean;
  lastLoginAt?: string;
  createdAt: string;
}

export function getFullName(user: Pick<IUser, 'firstName' | 'lastName'>): string {
  return `${user.firstName} ${user.lastName}`.trim();
}

export function getInitials(user: Pick<IUser, 'firstName' | 'lastName'>): string {
  return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
}
