/** DTO envoyé lors de la mise à jour du profil de l'utilisateur courant. */
export interface ProfileUpdateDto {
  firstName: string;
  lastName: string;
  email: string;
}

/** DTO envoyé lors du changement de mot de passe de l'utilisateur courant. */
export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}
