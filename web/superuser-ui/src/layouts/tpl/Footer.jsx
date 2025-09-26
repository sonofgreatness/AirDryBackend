const Footer = () => {
  return (
    <footer className="app-footer">
      <div className="footer-content">
        {/* Left Section */}
        <div className="footer-left">
          <span>
            Powered by{" "}
            <a href="mailto:simphiweiq@gmail.com" className="footer-link">
              Simphiwe Magagula
            </a>
          </span>
        </div>

        {/* Center Section */}
        <div className="footer-center">
          <a
            href="https://facebook.com/SimphiweMagagula"
            target="_blank"
            rel="noopener noreferrer"
            className="footer-link"
          >
            Facebook
          </a>
        </div>

        {/* Right Section */}
        <div className="footer-right">
          <a href="tel:+26876911464" className="footer-link">
            +268 7691 1464
          </a>
          <span className="footer-separator">|</span>
          <a href="tel:+26879487461" className="footer-link">
            +268 7948 7461
          </a>
        </div>
      </div>
    </footer>
  );
};
export default Footer;
