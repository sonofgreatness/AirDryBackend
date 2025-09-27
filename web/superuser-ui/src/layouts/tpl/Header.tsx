import { useEffect } from "react";
import { Link } from "react-router-dom";
import util from "../../util/util";
import { Dropdown } from "antd";
import { Menu, User, LogOut, ChevronDown } from "lucide-react";
import userIcon from "../../assets/icons/user-solid1.svg";
import $ from "jquery";
import { toggleResponsiveSidebarCollapse } from "../../util/util";
import { AuthService } from "../../services/AuthService";

export default function Header() {
  // ✅ Desktop sidebar toggle (only works on large screens)
  const toggleSidebarCollapse = (e: React.MouseEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (window.innerWidth > 767) {
      $("body").toggleClass("page-sidebar-closed");
    }
  };


  useEffect(() => {
    // Any initialization logic can go here
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
      <div
        className="d-flex align-items-center h-100 px-3"
        style={{
          backgroundColor: "#355E3B",
          width: "100vw",
        }}
      >
        {/* Logo + Sidebar Toggle */}
        <div className="d-flex align-items-center gap-3 flex-grow-1">
          
          <div className="menu-toggler sidebar-toggler text-white" onClick={toggleSidebarCollapse}>
            <Menu size={20} />
          </div>

          <div className="text-white fw-semibold d-none d-md-block">
            Inventory Management System
          </div>
        </div>

        {/* Right Side */}
        <div className="d-flex align-items-center gap-3">
          <Dropdown placement="bottomRight" trigger={["click"]} menu={{ items: userDDItems }}>
            <div className="cpointer d-flex align-items-center bg-light rounded-pill px-3 py-1">
              <img src={userIcon} alt="user" height={24} />
              <div className="d-flex align-items-center ms-2">
                <span
                  className="fw-bold text-dark me-1"
                  style={{
                    maxWidth: 100,
                    whiteSpace: "nowrap",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                  }}
                >
                  {util.geLoggedinName()}
                </span>
                <ChevronDown size={16} />
              </div>
            </div>
          </Dropdown>

          {/* Mobile Toggle */}
          <a href="/" className="d-md-none ms-2" onClick={toggleResponsiveSidebarCollapse}>
            <Menu size={22} color="white" />
          </a>
        </div>
      </div>
    </div>
  );
}
