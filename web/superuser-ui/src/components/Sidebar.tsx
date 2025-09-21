import { Link, useLocation } from "react-router-dom";

const menuItems = [
  { name: "Dashboard", path: "/" },
  { name: "Users", path: "/users" },
  { name: "Organizations", path: "/organizations" },
  { name: "Settings", path: "/settings" },
];

export default function Sidebar() {
  const location = useLocation();

  return (
    <aside className="bg-gray-900 text-white w-64 min-h-screen flex flex-col p-6">
      <h1 className="text-3xl font-bold mb-10 mt-2">Admin 6Panel</h1>
      <nav className="flex flex-col space-y-3">
        {menuItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`p-3 rounded hover:bg-gray-700 transition ${
              location.pathname === item.path ? "bg-gray-700" : ""
            }`}
          >
            {item.name}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
