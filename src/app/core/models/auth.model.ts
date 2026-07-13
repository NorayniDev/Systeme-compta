import { IUser } from './user.model';

export interface ILoginRequest {
  email: string;
  password: string;
  rememberMe: boolean;
}

export interface IAuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface ILoginResponse extends IAuthTokens {
  user: IUser;
}

export interface IRefreshTokenRequest {
  refreshToken: string;
}

export interface IForgotPasswordRequest {
  email: string;
}

export interface IResetPasswordRequest {
  token: string;
  newPassword: string;
}

/**
 * Payload décodé d'un access token JWT (claims utiles côté front uniquement —
 * ne jamais faire confiance à ces données pour des décisions de sécurité serveur).
 */
export interface IJwtPayload {
  sub: string;
  email: string;
  role: string;
  permissions: string[];
  iat: number;
  exp: number;
}
