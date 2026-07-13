import { UserRole } from '../../../core/constants/roles.constant';

/**
 * DTO envoyé lors de la création d'un utilisateur. Les permissions ne sont
 * pas transmises par le client : elles sont dérivées côté backend à partir
 * du rôle (voir `ROLE_PERMISSIONS` pour la matrice de repli côté frontend).
 */
export interface UserAccountCreateDto {
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  temporaryPassword: string;
}

/** DTO envoyé lors de la mise à jour d'un utilisateur. */
export interface UserAccountUpdateDto {
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  isActive: boolean;
}
