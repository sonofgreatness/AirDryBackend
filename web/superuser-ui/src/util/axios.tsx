import axios, { type AxiosInstance } from "axios";
import util from "../util/util";

let axiosBaseUrl = "/";
if (window.location.href.includes("localhost")) {
  axiosBaseUrl = `${window.location.protocol}//${window.location.hostname}:9090`;
} else {
  axiosBaseUrl = window.location.origin;
}

const axiosInstance: AxiosInstance = axios.create({
  baseURL: axiosBaseUrl,
    timeout: 900000,
  withCredentials:true,
});

// Request interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    const token = util.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
        config.headers.timezoneoffset = util.getTimezoneOffset();
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    util.handleAxiosError(error); // delegate to util
    return Promise.reject(error);
  }
);

export default axiosInstance;
