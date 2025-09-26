import { Routes, Route } from "react-router-dom";
import DefaultLayout from "./layouts/Default";
import Dashboard from "./pages/Dashboard";
import Organization from "./pages/Organizations";
import Settings from "./pages/Settings";
import Users from "./pages/Users";
import UserForm from "./pages/User/UserForm";
import Login from "./layouts/Login";
import Profile from "./pages/User/Profile";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<DefaultLayout><Dashboard /></DefaultLayout>} />
      <Route path="/dashboard" element={<DefaultLayout><Dashboard /></DefaultLayout>} />
      <Route path="/settings" element={<DefaultLayout><Settings /></DefaultLayout>} />
      <Route path="/users" element={<DefaultLayout><Users /></DefaultLayout>} />
      <Route path="/users/create" element={<DefaultLayout><UserForm /></DefaultLayout>} />
      <Route path="/users/:id/edit" element={<DefaultLayout><UserForm /></DefaultLayout>} />
      <Route path="/organizations" element={<DefaultLayout><Organization /></DefaultLayout>} />
      <Route path="/profile" element={<DefaultLayout><Profile /></DefaultLayout>} />
    </Routes>
  );
}
