import {useEffect} from "react";
import {Link} from "react-router-dom";
import bell from "../../assets/icons/bell-solid.svg";
import user from "../../assets/icons/user-solid.svg";
import gear from "../../assets/icons/gear-solid.svg";
import plus from "../../assets/icons/plus-solid.svg";
import $ from "jquery";

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
    $("body").removeClass("page-sidebar-mobile-offcanvas-open");
    $(".page-sidebar-cover").hide();
    console.log("close sidebare called")
  };

  const init = () => {
    let wh = $(window).height();
    if (wh != undefined) {
      let h = wh - 0;
      $(".sidebarScrollDiv").css({ height: h + "px" });
    }
    else { 
      console.log("wh undefined")
    }
  };

  useEffect(() => {
    init();
    $(window).off("resize");
    $(window).resize(() => {
      init();
    });
  }, []);

  return (
    <>
      <div className="page-sidebar-cover" onClick={(e) => closeResponsiveSidebar(e)}
      ></div>
      <div className="page-sidebar-wrapper d-print-none">
        <div className="page-sidebar navbar-collapse1 collapse1">
          <div className="sidebarScrollDiv">
            <ul
              className="page-sidebar-menu page-header-fixed"
              data-keep-expanded="false"
              data-auto-scroll="true"
              data-slide-speed="200"
              onClick={(e) => setNavActive(e)}
            >
              <li className="sidebar-mobile-offcanvas-toggler ">
                <a
                  href="/"
                  className="responsive-toggler"
                  onClick={(e) => closeResponsiveSidebar(e)}
                >
                  <i className="icon-logout"></i>
                </a>
              </li>

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
          </div>
        </div>
      </div>
    </>
  );
}
