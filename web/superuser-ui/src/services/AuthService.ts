import axiosInstance from "../util/axios";
import util from "../util/util";

export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export const AuthService = {
  async login(req: AuthRequest): Promise<AuthResponse> {
    const { data } = await axiosInstance.post<AuthResponse>("/users/login", req);
    util.setToken(data.token);
    return data;
  },

  logout(): void {
    console.log("logout called")
    util.clearToken();
    window.location.href = "/login";
  },

  async getCurrentUser() {
    const { data } = await axiosInstance.get("/users/current");
    return data;
  },
};
