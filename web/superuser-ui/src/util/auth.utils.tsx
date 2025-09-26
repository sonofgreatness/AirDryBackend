import { AuthService } from '../services/AuthService';

export const getLoggedInProfile = async () => {
  try {
    const user = await AuthService.getCurrentUser();
    return user;
  } catch {
    return null;
  }
};