import { useEffect } from "react";
import { Link } from "react-router-dom";
import util from "../../util/util";
import { Dropdown } from "antd";
import { User, LogOut, ChevronDown } from "lucide-react";
import userIcon from "../../assets/icons/user-solid1.svg";
import { AuthService } from "../../services/AuthService";
import {getLoggedInProfile} from "../../util/auth.utils"
import type { CurrentUserDTO } from "../../util/types/user";
import { useState } from "react";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";
export default function Header() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<CurrentUserDTO | null>(null);


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
     
      }
    };

    fetchProfile();
  }, []);
  
  useEffect(() => {
  }, []);

  const userDDItems = [
    {
      label: (
        <Link to="/profile" className="d-flex align-items-center gap-2">
          <User size={16} /> My Profile
        </Link>
      ),
      key: "1",
    },
    {
      label: (
        <div
          className="cursor-pointer d-flex align-items-center gap-2"
          onClick={() => AuthService.logout()}
        >
          <LogOut size={16} /> Logout
        </div>
      ),
      key: "2",
    },
  ];

  return (
<div className="page-header navbar navbar-fixed-top d-print-none bg-dark shadow-sm">
  <div className="d-flex align-items-center h-100 px-3" style={{ backgroundColor: "#355E3B", width: "100vw" }}>
    <div className="d-flex align-items-center gap-3 flex-grow-1">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="btn btn-sm btn-light d-flex align-items-center gap-1"
      >
        <ArrowLeft size={16} /> Back
      </button>

      <div className="text-white fw-semibold d-none d-md-block">
        BrotherHood Portal
      </div>
    </div>

    <div className="d-flex align-items-center gap-3">
      <Dropdown placement="bottomRight" trigger={["click"]} menu={{ items: userDDItems }}>
        <div className="cpointer d-flex align-items-center bg-light rounded-pill px-3 py-1">
          <img src={userIcon} alt="user" height={24} />
          <div className="d-flex align-items-center ms-2">
            <span className="fw-bold text-dark me-1" style={{ maxWidth: 100, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
              {profile?.name}
            </span>
            <ChevronDown size={16} />
          </div>
        </div>
      </Dropdown>
    </div>
  </div>
</div>
  );
}
