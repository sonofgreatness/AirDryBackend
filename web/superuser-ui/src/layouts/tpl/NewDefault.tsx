import React, { useEffect } from "react";
import Header from "./NewHeader";
import Footer from "./Footer";
import util from "../../util/util"
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
      <div>
        {/* Main content area */}
        <div>
          <div>{children}</div>
        </div>
      </div>
      <Footer />
    </>
  );
}
