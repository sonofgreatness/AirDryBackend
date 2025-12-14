import { Link } from "react-router-dom";


const Footer = () => {
  return (
<footer className="footer-section mt-5 py-4">
  <div className="container">

    <div className="row text-center text-md-start">

      {/* CONTACT SECTION */}
      <div className="col-md-4 mb-4">
        <h4 className="footer-title">Contact</h4>

        <p className="footer-link">
          📧 <a href="mailto:simphiweiq@gmail.com" target="_blank" rel="noopener noreferrer">
   simphiweiq@gmail.com
</a>
        </p>

        <p className="footer-link">
          📞 <a href="tel:+26876911464">+268 7691 1464</a>
        </p>

        <p className="footer-link">
          📞 <a href="tel:+26879487461">+268 7948 7461</a>
        </p>
      </div>

      {/* PLATFORM LINKS */}
      <div className="col-md-4 mb-4">
        <h4 className="footer-title">Platforms</h4>

        <p className="footer-link">
          🔐 <Link to="/login">Login to Brotherhood Portal</Link>
        </p>

        <p className="footer-link">
          📊 <a 
                href="http://dash.localhost" 
                target="_blank" 
                rel="noopener noreferrer"
              >
                Login to Intelligence Platform
              </a>
        </p>
      </div>

      {/* BRANDING / MESSAGE */}
      <div className="col-md-4 mb-4">
        <h4 className="footer-title">Brotherhood System</h4>
        <p className="footer-desc">
          Built with modern, resilient, open technology  
          to ensure fairness, transparency, and dignity  
          for every worker in the timber industry.
            </p>
              <p className="footer-link">
         🌲 <Link to="/manifesto">View Manifesto</Link>
            </p>
            <p className="footer-link">
          ⇓ <Link to="/releases">Get Releases</Link>
        </p>
      </div>

    </div>

    <div className="footer-bottom text-center mt-3">
          © {new Date().getFullYear()} Brotherhood of Timber Workers.  
          Technology with Soul.
      <br></br>    by Simphiwe Magagula.
    </div>
  </div>
</footer>
  );
};
export default Footer;
