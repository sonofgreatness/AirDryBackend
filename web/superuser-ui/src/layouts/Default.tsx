import React, { useEffect } from "react";
import Header from "./tpl/NewHeader";
import Footer from "./tpl/Footer";
import util from "../util/util"
import {useNavigate} from "react-router-dom";

type DefaultLayoutProps = {
  children: React.ReactNode;
};
export default function Default({ children }: DefaultLayoutProps) {
  const navigate = useNavigate();
  useEffect(() => {
    if (!util.getToken() || util.isTokenExpired()) {
      navigate("/login");
    }
  }, [navigate]);

  return (
    <>
      <Header />
      <div className="page-container d-flex">
        {/* Sidebar */}
  
        {/* Main content area */}
        <div className="page-content flex-grow-1">
          <div className="page-content-wrapper">{children}</div>
        </div>
      </div>
      <Footer />
    </>
  );
}
