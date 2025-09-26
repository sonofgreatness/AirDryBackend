import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthService, type AuthRequest } from "../services/AuthService";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

export default function Login() {
  const [form, setForm] = useState<AuthRequest>({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    console.log("before prevent default")
    e.preventDefault(); // prevent page reload
    console.log("after prevent default")
    if (loading) {
      console.log("loading true")
      return;
    } // prevent double submit
    setLoading(true);

    try {
      console.log("try")
      await AuthService.login(form);
      toast.success("Login successful!", {
        position: "top-right",
        autoClose: 2000, // readable duration
        hideProgressBar: false,
        pauseOnHover: true,
        closeOnClick: true,
        draggable: true,
      });

      // Delay navigation slightly to allow user to see the toast
      setTimeout(() => {
        console.log("moving to dashboard")
        navigate("/dashboard");
      }, 5000); // 0.5s delay
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
        autoClose: 5000, // show error long enough
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

  return (
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
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            className="form-control"
            placeholder="Password"
            required
          />
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
  );
}
