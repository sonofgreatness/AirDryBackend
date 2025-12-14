import { AuthService } from '../services/AuthService';
import type { CurrentUserDTO } from "../util/types/user"
export const getLoggedInProfile = async () => {
  try {
    const user = await AuthService.getCurrentUser();
    return user;
  } catch {
    return null;
  }
};

/**/
export const isSuperUser = async () => {
  try {
    const user:CurrentUserDTO = await AuthService.getCurrentUser();
    return (user.userTypeName === "super_user") 
  } catch {
    return false;
  }
}; 
