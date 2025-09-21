import React, { useEffect } from "react";
import Header from "./tpl/Header";
import Leftnavs from "./tpl/Leftnavs";
import Footer from "./tpl/Footer";

type DefaultLayoutProps = {
  children: React.ReactNode;
};

export default function Default({ children }: DefaultLayoutProps) {
  useEffect(() => {
    // Example auth check
    // if (!util.isLogged()) navigate('/');
  }, []);

  return (
    <>
      <Header />
      <div className="page-container d-flex">
        {/* Sidebar */}
        <Leftnavs />

        {/* Main content area */}
        <div className="page-content flex-grow-1">
          <div className="page-content-wrapper">{children}</div>
        </div>
      </div>
      <Footer />
    </>
  );
}
