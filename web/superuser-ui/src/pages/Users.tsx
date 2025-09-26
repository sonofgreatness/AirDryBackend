// src/pages/User.tsx
import React, { useEffect, useState } from "react";
import { UserService } from "../services/UserService";
import { useNavigate } from "react-router-dom";
import { type UserResponse, type UserTypesDTO } from "../util/types/user";
import { type PaginatedResponse } from "../util/types/common";

const pageSize = 10;

const UserPage: React.FC = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Primary filters
  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");

  // Advanced filters
  const [organizationId, setOrganizationId] = useState("");
  const [departmentId, setDepartmentId] = useState("");
  const [sectionId, setSectionId] = useState("");
  const [userTypeId, setUserTypeId] = useState("");

  // Dropdown data
  const [userTypes, setUserTypes] = useState<UserTypesDTO[]>([]);
  // TODO: Replace with API once endpoints exist
  const [organizations, setOrganizations] = useState<{ id: string; displayName: string }[]>([]);
  const [departments, setDepartments] = useState<{ id: string; displayName: string }[]>([]);
  const [sections, setSections] = useState<{ id: string; displayName: string }[]>([]);

  const [showAdvanced, setShowAdvanced] = useState(false);

  async function loadUsers() {
    setLoading(true);
    setError(null);
    try {
      const filters = {
        name: name || undefined,
        surname: surname || undefined,
        email: email || undefined,
        phoneNumber: phone || undefined,
        organizationId: organizationId || undefined,
        departmentId: departmentId || undefined,
        sectionId: sectionId || undefined,
        userTypeId: userTypeId || undefined,
      };

      const res: PaginatedResponse<UserResponse> =
        await UserService.getUsersWithFilters(page, pageSize, filters);

      setUsers(res.items);
      setTotalPages(res.totalPages ?? 1);
      setTotalItems(res.totalCount ?? res.items.length);
    } catch (err: any) {
      console.error("Failed to load users", err);
      setError("Failed to load users. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, [page]);

  useEffect(() => {
    // Preload dropdowns
    UserService.getUserTypes().then(setUserTypes).catch(console.error);

    // TODO: Replace with API requests
    setOrganizations([
      { id: "org-1", displayName: "Org One" },
      { id: "org-2", displayName: "Org Two" },
    ]);
    setDepartments([
      { id: "dept-1", displayName: "Department A" },
      { id: "dept-2", displayName: "Department B" },
    ]);
    setSections([
      { id: "sec-1", displayName: "Section X" },
      { id: "sec-2", displayName: "Section Y" },
    ]);
  }, []);

  const handleFilterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    loadUsers();
  };

  return (
    <div className="user-page">
      <h1 className="title">User Management</h1>

      <form className="filter-form" onSubmit={handleFilterSubmit}>
        {/* Primary filters */}
        <input
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          placeholder="Surname"
          value={surname}
          onChange={(e) => setSurname(e.target.value)}
        />
        <input
          placeholder="Email (Username)"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          placeholder="Phone"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
        />

        <button type="submit" className="gradient-btn-small">
          Apply Filters
        </button>

        {/* Collapsible toggle */}
        <button
          type="button"
          className="ellipsis-btn"
          onClick={() => setShowAdvanced((prev) => !prev)}
        >
          ⋮
        </button>
      </form>

      {/* Advanced filters */}
      {showAdvanced && (
        <div className="advanced-filters">
          <select
            value={organizationId}
            onChange={(e) => setOrganizationId(e.target.value)}
          >
            <option value="">Select Organization</option>
            {organizations.map((org) => (
              <option key={org.id} value={org.id}>
                {org.displayName}
              </option>
            ))}
          </select>

          <select
            value={departmentId}
            onChange={(e) => setDepartmentId(e.target.value)}
          >
            <option value="">Select Department</option>
            {departments.map((dep) => (
              <option key={dep.id} value={dep.id}>
                {dep.displayName}
              </option>
            ))}
          </select>

          <select
            value={sectionId}
            onChange={(e) => setSectionId(e.target.value)}
          >
            <option value="">Select Section</option>
            {sections.map((sec) => (
              <option key={sec.id} value={sec.id}>
                {sec.displayName}
              </option>
            ))}
          </select>

          <select
            value={userTypeId}
            onChange={(e) => setUserTypeId(e.target.value)}
          >
            <option value="">Select User Type</option>
            {userTypes.map((ut) => (
              <option key={ut.id} value={ut.id}>
                {ut.displayName}
              </option>
            ))}
          </select>
        </div>
      )}

      {loading && (
        <div className="loader">
          <div className="spinner"></div>
          <p>Loading users...</p>
        </div>
      )}
      {error && <p className="error">{error}</p>}

      {!loading && !error && (
        <>
          <table className="user-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Surname</th>
                <th>Email (Username)</th>
                <th>Phone</th>
                <th>Status</th>
                <th>Type</th>
                <th>Organization</th>
                <th>Department</th>
                <th>Section</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan={9} className="no-users">
                    No users found
                  </td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.name}</td>
                    <td>{user.surname}</td>
                    <td>{user.email ?? "-"}</td>
                    <td>{user.phoneNumber}</td>
                    <td>
                      {user.isActive ? "✅ Active" : "❌ Inactive"} /{" "}
                      {user.isApproved ? "Approved" : "Pending"}
                    </td>
                    <td>{user.userType?.displayName ?? "-"}</td>
                    <td>{user.organization?.displayName ?? "-"}</td>
                    <td>{user.department?.displayName ?? "-"}</td>
                    <td>{user.section?.displayName ?? "-"}</td>
                    <td>
                      <button
                        type="button"
                        onClick={() => navigate(`/users/${user.id}/edit`)}
                        className="ticket-btn"
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          <div className="pagination-summary">
            Page {page} of {totalPages} — {totalItems} users total
          </div>

          <div className="pagination">
            <button
              disabled={page === 1}
              onClick={() => setPage((prev) => Math.max(prev - 1, 1))}
            >
              Previous
            </button>
            <span>{page}</span>
            <button
              disabled={page === totalPages}
              onClick={() => setPage((prev) => Math.min(prev + 1, totalPages))}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default UserPage;
