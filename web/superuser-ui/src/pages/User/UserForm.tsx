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
  const [organization, setOrganizations] = useState<UserTypesDTO[]>([]);
  const [loading, setLoading] = useState(isEdit);
  const [error, setError] = useState<string | null>(null);

  // Fetch user types
  useEffect(() => {
    UserService.getUserTypes().then(setUserTypes).catch((e) => setError(`Failed to load user types: ${e.message}`));
    UserService.getOrganizations().then(setOrganizations).catch((e) => setError(`Failed to load organizations: ${e.message}`));
    
  }, []);

  // Preload user if editing
  useEffect(() => {
    if (isEdit) {
      setLoading(true);
      UserService.getUser(id!)
        .then((user) => {
          setForm({
            updated_name: user.name,
            updated_surname: user.surname,
            updated_email: user.email ?? "",
            updated_phoneNumber: user.phoneNumber,
            updated_secondaryPhoneNumber: user.secondaryPhoneNumber ?? "",
            updated_userTypeId: user.userTypeId ?? "",
            updated_departmentId: user.departmentId ?? "",
            updated_sectionId: user.sectionId ?? "",
            isActive: user.isActive,
            isApproved: user.isApproved,
          });
        })
        .catch((e) => setError(`Failed to load user: ${e.message}`))
        .finally(() => setLoading(false));
    } else { 
      setLoading(false);
      setForm({
        name: "",
        surname: "",
        email:"",
        phoneNumber: "",
        secondaryPhoneNumber: "",
        userTypeId:"",
        organizationId:"",
        isActive:false,
        isApproved:false,
      }); 
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
            {isEdit ?
              <input
                className="p-2 border rounded"
                placeholder="Name"
                value={form.updated_name || ""}
                onChange={(e) => setForm({ ...form, updated_name: e.target.value })}
              /> :
              <input
                className="p-2 border rounded"
                placeholder="Name"
                value={form.name || ""}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
            }
            {isEdit ?
              <input
                className="p-2 border rounded"
                placeholder="Surname"
                value={form.updated_surname || ""}
                onChange={(e) => setForm({ ...form, updated_surname: e.target.value })}
              />
              :
              <input
                className="p-2 border rounded"
                placeholder="Surname"
                value={form.surname || ""}
                onChange={(e) => setForm({ ...form, surname: e.target.value })}
              />
            }
            {isEdit ?

              <input
                className="p-2 border rounded"
                placeholder="Email"
                value={form.updated_email || ""}
                onChange={(e) => setForm({ ...form, updated_email: e.target.value })}
              /> :
              <input
                className="p-2 border rounded"
                placeholder="Email"
                value={form.email || ""}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            }
            {isEdit ?
              <input
                className="p-2 border rounded"
                placeholder="Phone"
                value={form.updated_phoneNumber || ""}
                onChange={(e) => setForm({ ...form, updated_phoneNumber: e.target.value })}
              /> :
              <input
                className="p-2 border rounded"
                placeholder="Phone"
                value={form.phoneNumber || ""}
                onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
              />
            }
            {isEdit ?
              <input
                className="p-2 border rounded"
                placeholder="Secondary Phone"
                value={form.updated_secondaryPhoneNumber || ""}
                onChange={(e) =>
                  setForm({ ...form, updated_secondaryPhoneNumber: e.target.value })
                }
              /> :
              <input
                className="p-2 border rounded"
                placeholder="Secondary Phone"
                value={form.secondaryPhoneNumber || ""}
                onChange={(e) =>
                  setForm({ ...form, secondaryPhoneNumber: e.target.value })
                } />
            }
            {isEdit ?
              <select
                className="p-2 border rounded"
                value={form.updated_userTypeId || ""}
                onChange={(e) => setForm({ ...form, updated_userTypeId: e.target.value })}
              >
                <option value="">Select User Type</option>
                {userTypes.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.displayName}
                  </option>
                ))}
              </select> :
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

            }
            {isEdit ?
              <input
                type="password"
                className="p-2 border rounded"
                placeholder="Password"
                value={form.updated_password || ""}
                onChange={(e) => setForm({ ...form, updated_password: e.target.value })}
              /> :
              <input
                type="password"
                className="p-2 border rounded"
                placeholder="Password"
                value={form.password || ""}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            }
            {!isEdit && (
              <select
                className="p-2 border rounded"
                value={form.organizationId || ""}
                onChange={(e) => setForm({ ...form, organizationId: e.target.value })}
              >
                <option value="">Select Organization</option>
                {organization.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.displayName}
                  </option>
                ))}
              </select>
            )
            }

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
