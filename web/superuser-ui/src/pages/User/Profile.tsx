import { useEffect, useState } from "react";
import util from "../../util/util";
import {getLoggedInProfile} from "../../util/auth.utils"
interface CurrentUserDTO {
  id: string;
  name: string;
  surname: string;
  email?: string;
  phoneNumber: string;
  userTypeId?: string;
  userTypeName?: string;
  organizationId?: string;
  organizationName?: string;
  departmentId?: string;
  departmentName?: string;
  sectionId?: string;
  sectionName?: string;
}

export default function Profile() {
  const [profile, setProfile] = useState<CurrentUserDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        // Call the backend to get the logged-in user
        const data = await getLoggedInProfile();
        setProfile(data);
      } catch (error) {
        console.error("Error fetching profile:", error);
        util.showAlertMsg("Failed to load profile", "E");
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  if (loading) {
    return (
      <div className="page-content page-pad">
        <h3>Loading profile...</h3>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="page-content page-pad">
        <h3>No profile data available</h3>
      </div>
    );
  }

  return (
    <div className="page-content page-pad">
      <div className="page-head uc d-flex mb-4">
        <div className="my-auto">
          <h2>My Profile</h2>
        </div>
      </div>

      <div className="card p-4 shadow-sm">
        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Full Name:</div>
          <div className="col-md-9">{profile.name} {profile.surname}</div>
        </div>

        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Email:</div>
          <div className="col-md-9">{profile.email || "N/A"}</div>
        </div>

        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Phone Number:</div>
          <div className="col-md-9">{profile.phoneNumber}</div>
        </div>

        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">User Type:</div>
          <div className="col-md-9">{profile.userTypeName || "N/A"}</div>
        </div>

        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Organization:</div>
          <div className="col-md-9">{profile.organizationName || "N/A"}</div>
        </div>

        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Department:</div>
          <div className="col-md-9">{profile.departmentName || "N/A"}</div>
        </div>

        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Section:</div>
          <div className="col-md-9">{profile.sectionName || "N/A"}</div>
        </div>
      </div>
    </div>
  );
}
