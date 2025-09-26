// src/pages/UserForm.tsx
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { UserService } from "../../services/UserService";
import {
  type UserRegisterRequest,
  type UpdateUserDTO,
  type UserTypesDTO,
} from "../../util/types/user";

export default function UserForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [form, setForm] = useState<
    Partial<UserRegisterRequest & UpdateUserDTO>
  >({});
  const [userTypes, setUserTypes] = useState<UserTypesDTO[]>([]);
  const [loading, setLoading] = useState(isEdit);
  const [error, setError] = useState<string | null>(null);

  // Fetch user types
  useEffect(() => {
    UserService.getUserTypes()
      .then(setUserTypes)
      .catch((e) => setError(`Failed to load user types: ${e.message}`));
  }, []);

  // Preload user if editing
  useEffect(() => {
    if (isEdit) {
      setLoading(true);
      UserService.getUser(id!)
        .then((user) => {
          setForm({
            id: user.id,
            name: user.name,
            surname: user.surname,
            email: user.email ?? "",
            phoneNumber: user.phoneNumber,
            secondaryPhoneNumber: user.secondaryPhoneNumber ?? "",
            userTypeId: user.userTypeId ?? "",
            organizationId: user.organizationId ?? "",
            departmentId: user.departmentId ?? "",
            sectionId: user.sectionId ?? "",
            isActive: user.isActive,
            isApproved: user.isApproved,
          });
        })
        .catch((e) => setError(`Failed to load user: ${e.message}`))
        .finally(() => setLoading(false));
    }
  }, [id, isEdit]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      if (isEdit) {
        await UserService.updateUser(id!, form as UpdateUserDTO);
      } else {
        await UserService.registerUser(form as UserRegisterRequest);
      }
      navigate("/users");
    } catch (e: any) {
      setError(`Failed to save user: ${e.message}`);
    }
  }

  if (loading) {
    return (
      <div className="user-form-page">
        <div className="user-form-card">Loading user...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="user-form-page">
        <div className="user-form-card text-red-600">
          <p>{error}</p>
          <button onClick={() => navigate("/users")} className="ticket-btn mt-4">
            Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="user-form-page">
      <div className="user-form-card">
        <h2 className="form-title">{isEdit ? "Edit User" : "Create User"}</h2>
        <form onSubmit={handleSubmit} className="form-grid">
          <input
            className="p-2 border rounded"
            placeholder="Name"
            value={form.name || ""}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
          <input
            className="p-2 border rounded"
            placeholder="Surname"
            value={form.surname || ""}
            onChange={(e) => setForm({ ...form, surname: e.target.value })}
          />
          <input
            className="p-2 border rounded"
            placeholder="Email"
            value={form.email || ""}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
          <input
            className="p-2 border rounded"
            placeholder="Phone"
            value={form.phoneNumber || ""}
            onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
          />
          <input
            className="p-2 border rounded"
            placeholder="Secondary Phone"
            value={form.secondaryPhoneNumber || ""}
            onChange={(e) =>
              setForm({ ...form, secondaryPhoneNumber: e.target.value })
            }
          />

          <select
            className="p-2 border rounded"
            value={form.userTypeId || ""}
            onChange={(e) => setForm({ ...form, userTypeId: e.target.value })}
          >
            <option value="">Select User Type</option>
            {userTypes.map((t) => (
              <option key={t.id} value={t.id}>
                {t.displayName}
              </option>
            ))}
          </select>

          {!isEdit && (
            <input
              type="password"
              className="p-2 border rounded"
              placeholder="Password"
              value={form.password || ""}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          )}

          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={!!form.isActive}
              onChange={(e) => setForm({ ...form, isActive: e.target.checked })}
            />
            Active
          </label>
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={!!form.isApproved}
              onChange={(e) =>
                setForm({ ...form, isApproved: e.target.checked })
              }
            />
            Approved
          </label>

          <div className="form-actions">
            <button type="submit" className="ticket-btn">
              {isEdit ? "Update" : "Create"}
            </button>
            <button
              type="button"
              onClick={() => navigate("/users")}
              className="text-btn"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
