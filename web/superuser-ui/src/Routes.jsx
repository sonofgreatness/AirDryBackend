import { Routes, Route } from "react-router-dom";
import DefaultLayout from "./layouts/Default";
import Dashboard from "./pages/Dashboard";
import Organization from "./pages/Organizations";
import Settings from "./pages/Settings";
import Users from "./pages/Users";
import Login from "./layouts/Login";

export default function AppRoutes() {
  return (
    <Routes>
      {/* Login Page */}
     
      {/* Authenticated Pages */}

      <Route path="/" element={<DefaultLayout><Dashboard /></DefaultLayout>} />
      <Route path="/login" element={<DefaultLayout><Login /></DefaultLayout>} />
      <Route path="/dashboard" element={<DefaultLayout><Dashboard /></DefaultLayout>} />
      <Route path="/settings" element={<DefaultLayout><Settings /></DefaultLayout>} />
      <Route path="/users" element={<DefaultLayout><Users /></DefaultLayout>} />
      <Route path="/organizations" element={<DefaultLayout><Organization /></DefaultLayout>} />

      {/* Catch-All (Not Found) */}
     
    </Routes>
  );
}
