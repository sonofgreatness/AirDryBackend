import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthService, type AuthRequest } from "../services/AuthService";
import { ToastContainer, toast } from "react-toastify";
import Footer from "../layouts/tpl/Footer"
import {isSuperUser} from "../util/auth.utils"
import "react-toastify/dist/ReactToastify.css";

export default function Login() {
  const [form, setForm] = useState<AuthRequest>({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (loading) {
      return;
    }
    setLoading(true);
    try {
      await AuthService.login(form);
      toast.success("Login successful!", {
        position: "top-right",
        autoClose: 200,
        hideProgressBar: false,
        pauseOnHover: true,
        closeOnClick: true,
        draggable: true,
      });
      setTimeout(() => {
       moveToNextPage()
      
      }, 500); // 0.5s delay
    } catch (error: any) {
      console.log("catch")
      let message = "Login failed. Please try again.";

      if (error?.response) {
        switch (error.response.status) {
          case 401:
            message = "Invalid username or password.";
            break;
          case 403:
            message = "User not active or approved.";
            break;
          case 500:
            message = "Server error. Please try later.";
            break;
          default:
            message = error.response.data?.message || message;
        }
      }

      toast.error(message, {
        position: "top-right",
        autoClose: 1000, 
        hideProgressBar: false,
        pauseOnHover: true,
        closeOnClick: true,
        draggable: true,
      });
    } finally {
      console.log("finally")
      setLoading(false);
    }
  };

  const moveToNextPage = async () => { 
    try {
      let dummy = await isSuperUser()
      if (dummy) { navigate("/users"); }
      else {navigate("/user_stats"); }
     }
    catch (error:any) { 
          throw error
    }
  }

  return (
    <>
    <div className="login-page d-flex justify-content-center align-items-center vh-100">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-4 shadow rounded"
        style={{ width: 320 }}
      >
        <h3 className="text-center mb-4" style={{ color: "var(--green-main)" }}>
          Sign In
        </h3>

        <div className="mb-3">
          <input
            type="text"
            name="username"
            value={form.username}
            onChange={handleChange}
            className="form-control"
            placeholder="Username"
            required
          />
        </div>

       <div className="mb-3">
  <div className="input-group">
    <input
      type={showPassword ? "text" : "password"}
      name="password"
      value={form.password}
      onChange={handleChange}
      className="form-control"
      placeholder="Password"
      required
    />
    <button
      type="button"
      className="btn btn-outline-secondary"
      onClick={() => setShowPassword(!showPassword)}
    >
      {showPassword ? "Hide" : "Show"}
    </button>
  </div>
</div>

        <button
          type="submit"
          disabled={loading}
          className="btn w-100"
          style={{ backgroundColor: "var(--green-main)", color: "white" }}
        >
          {loading ? "Signing in..." : "Login"}
        </button>
      </form>

      {/* Toast container must be included once in your app */}
      <ToastContainer />
          
    </div>
        <Footer />
        </>

  );
}
