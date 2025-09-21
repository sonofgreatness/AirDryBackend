export default function Navbar() {
  return (
    <header className="bg-white shadow px-6 py-4 flex justify-between items-center sticky top-0 z-10">
      <h2 className="text-2xl font-semibold">Superuser Module</h2>
      <button className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition">
        Logout
      </button>
    </header>
  );
}
