import { useEffect } from "react";
import { Link } from "react-router-dom";
import { X } from "lucide-react";
import bell from "../../assets/icons/bell-solid.svg";
import user from "../../assets/icons/user-solid.svg";
import gear from "../../assets/icons/gear-solid.svg";
import plus from "../../assets/icons/plus-solid.svg";
import $ from "jquery";
import util from "../../util/util";

export default function Leftnavs() {
  const setNavActive = (e: React.MouseEvent<HTMLUListElement, MouseEvent>) => {
    $(".nav-item").removeClass("active");
    let li = $(e.target as HTMLElement).closest("li");
    if (!li.hasClass("sidebar-mobile-offcanvas-toggler")) {
      li.addClass("active");
      closeResponsiveSidebar();
    }
  };

  const closeResponsiveSidebar = (
    e?: React.MouseEvent<HTMLAnchorElement | HTMLDivElement, MouseEvent>
  ) => {
    if (e) e.preventDefault();

    if (window.innerWidth <= 991) {
      $("body").removeClass("page-sidebar-mobile-offcanvas-open");
      $(".page-sidebar-cover").hide();
    } else {
      $("body").toggleClass("page-sidebar-closed");
      $(".page-sidebar-cover").hide();
    }
  };

  const init = () => {
    let wh = $(window).height();
    if (wh !== undefined) {
      $(".sidebarScrollDiv").css({ height: wh + "px" });
    }
  };

  useEffect(() => {
    init();
    $(window).off("resize");
    $(window).resize(() => init());
  }, []);

  const isTokenValid = !util.isTokenExpired();

  return (
    <>
      {/* Cover for mobile mode */}
      <div className="page-sidebar-cover" onClick={(e) => closeResponsiveSidebar(e)}></div>

      <div className="page-sidebar-wrapper d-print-none">
        {/* 🔥 Top-left circular toggle button */}
        <div className="sidebar-toggle-btn gradient-btn" onClick={(e) => closeResponsiveSidebar(e)}>
          <X size={20} className="gradient-icon" />
        </div>

        <div className="page-sidebar navbar-collapse1 collapse1">
          <div className="sidebarScrollDiv">
            {isTokenValid ? (
              <ul
                className="page-sidebar-menu page-header-fixed"
                data-keep-expanded="false"
                data-auto-scroll="true"
                data-slide-speed="200"
                onClick={(e) => setNavActive(e)}
              >
                <li className="nav-item start">
                  <Link to="/dashboard" className="nav-link">
                    <img src={bell} alt="notifications" height={15} />
                    <span className="title">Dashboard</span>
                  </Link>
                </li>

                <li className="heading">
                  <h3 className="uppercase">Modules</h3>
                </li>

                <li className="nav-item">
                  <Link to="/users" className="nav-link">
                    <img src={user} alt="user" height={15} />
                    <span className="title">Users &amp; Users</span>
                  </Link>
                </li>

                <li className="nav-item">
                  <Link to="/settings" className="nav-link">
                    <img src={gear} alt="gear" height={15} />
                    <span className="title">Settings</span>
                  </Link>
                </li>

                <li className="nav-item">
                  <Link to="/organizations" className="nav-link">
                    <img src={plus} alt="plus" height={15} />
                    <span className="title">Organizations</span>
                  </Link>
                </li>
              </ul>
            ) : (
              <div className="login-helper-card">
                <h2 className="login-helper-title">Login Required</h2>
                <p className="login-helper-text">
                  Please log in to view your dashboard, manage users, and access settings.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
