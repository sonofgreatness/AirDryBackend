import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ReleaseService } from "../../services/ReleaseService";
import { type ReleaseDTO } from "../../util/types/common";

export default function ReleaseForm() {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEdit = !!id;
    const platforms = ["ANDROID", "WINDOWS"];
    const [error, setError] = useState<string | null>(null);
  
    const [form, setForm] = useState<
       Partial< ReleaseDTO>
    >();
    const [loading, setLoading] = useState(isEdit);
  

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        try {
            if (isEdit) {
                await ReleaseService.updateRelease(form as ReleaseDTO);
            } else {
                await ReleaseService.registerRelease(form as ReleaseDTO);
            }
            navigate("/releases/all");
        } catch (e: any) {
            setError(`Failed to save release: ${JSON.stringify(error)}`);
        }
    }
    useEffect(() => {
        if (isEdit) {
            setLoading(true)
            ReleaseService.loadRelease(id).then(
                (release) => {
                    setForm({
                        link: release.link,
                        id: release.id,
                        dateOfRelease: release.dateOfRelease,
                        platform: release.platform
                    });
                }
            ).catch((e) => setError(`Failed to load user: ${e.message}`))
                .finally(() => setLoading(false));

        } else {
            setForm({
                link: "",
                id: generateUUID(),
                dateOfRelease: "useless",
                platform: platforms[0]

            }
            );
        }
    }, [id, isEdit]);
    
  
    
if (loading) {
    return (
      <div className="user-form-page">
        <div className="user-form-card">Loading release...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="user-form-page">
        <div className="user-form-card text-red-600">
          <p>{error}</p>
          <button onClick={() => navigate("/releases/all")} className="ticket-btn mt-4">
            Back
          </button>
        </div>
      </div>
    );
  }
   
    return (
      <div className="user-form-page">
        <div className="user-form-card">
          <h2 className="form-title">{isEdit ? "Edit Release" : "Create Release"}</h2>
         
            <form onSubmit={handleSubmit} className="form-grid">
              <input
                className="p-2 border rounded"
                placeholder="link"
                value={form?.link || ""}
                onChange={(e) => setForm({ ...form, link: e.target.value })}
                    />
                    <label>Select Platform</label>
                 <select
                className="p-2 border rounded"
                value={form?.platform || ""}
                onChange={(e) => setForm({ ...form, platform: e.target.value })}
              >
            
                {platforms.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select> 
                  <div className="form-actions">
              <button type="submit" className="ticket-btn">
                {isEdit ? "Update" : "Create"}
              </button>
              <button
                type="button"
                onClick={() => navigate("/releases/all")}
                className="text-btn"
              >
                Cancel
              </button>
            </div>
            </form>   
        </div>
        </div>
  )  
}
export function generateUUID(): string { 
    return crypto.randomUUID(); 
} 