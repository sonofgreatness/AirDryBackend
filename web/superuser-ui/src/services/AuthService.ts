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
    window.location.href = "/";
  },

  async getCurrentUser() {
    const { data } = await axiosInstance.get("/users/current");
    return data;
  },
  async greetServer() {
    const { data} = await axiosInstance.get("/api/check_health_hala");
    console.log("hala at server success",JSON.stringify(data))
  }, 
  async greetServerFromGallery() { 
    const { data} = await axiosInstance.get("/api/check_health_gallery");
    console.log("hala at server from gallery  success",JSON.stringify(data))
    
  },
   async greetServerFromReleases() { 
    const { data} = await axiosInstance.get("/api/check_health_releases");
    console.log("hala at server from releases  success",JSON.stringify(data))
    
  }

};
