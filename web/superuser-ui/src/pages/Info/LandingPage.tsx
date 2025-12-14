import stack_column from "../../assets/icons/stack_column.jpg"
import costBenefit from "../../assets/icons/cost_benefit.png"
import reportFront from "../../assets/icons/front_report.png"
import androidWorkFlow from "../../assets/icons/android_workflow.png"
import desktopWorkFlow from "../../assets/icons/desktop_workflow.png"
import reportBack from "../../assets/icons/back_report.png" 
import { Link } from "react-router-dom";
import Footer from "../../layouts/tpl/Footer"
import { useState } from "react"
import { AuthService } from "../../services/AuthService"
import { useEffect } from "react"

export default function LandingPage() {


    useEffect(() => {
        AuthService.greetServer();
    }, []);
    // Count images
const IMAGES: string[] = [
  stack_column,
  costBenefit,
  reportFront,
  androidWorkFlow,
  desktopWorkFlow,
  reportBack
];
const [loadedCount, setLoadedCount] = useState<number>(0);
const allLoaded: boolean = loadedCount >= IMAGES.length;

  const handleImageLoaded = (): void => {
  setLoadedCount((/** @type {number} */ prev) => prev + 1);
  };
  
const [showVideoModal, setShowVideoModal] = useState(false);


  return (
    

    <div className="page-pad">
      
      {/* === GLOBAL LOADER OVERLAY === */}
      
      {!allLoaded && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(255, 255, 255, 0.8)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 9999,
          }}
        >
          <div className="ncaloader"></div>
        </div>
      )}

      {/* === Preload images invisibly === */}
      <div style={{ display: "none" }}>
        {IMAGES.map((src, i) => (
          <img key={i} src={src} onLoad={handleImageLoaded} alt="" />
        ))}
      </div>

      
      {allLoaded && (
        <div className="fade-in">
          {/* === INTRO SECTION === */}
          <section className="intro-section container mb-5">
            <div className="row align-items-center">

              {/* === IMAGE COLUMN === */}
              <div className="col-md-5 text-center mb-4 mb-md-0">
                <img
                  src={stack_column}
                  className="intro-image img-fluid"
                  alt="Brotherhood of Timber Workers"
                />
              </div>

              {/* === TEXT COLUMN === */}
              <div className="col-md-7">
                <h1 className="intro-title mb-3">
                  The Brotherhood of Timber Workers
                </h1>

                <p className="intro-quote mb-4">
                  “Those who love to work may sometimes forget that a successful
                  human community requires the performance of many vapid and
                  colorless tasks...”
                  <br />
                  <span className="quote-author">— Judge Walter Pettus Gewin</span>
                </p>

                <p className="intro-description">
                  This platform exists to ensure **fair, transparent and accurate
                  compensation** for every hour of honest labor.
                  Our suite of modern, resilient, and original digital tools
                  empowers timber workers, supervisors, and organizations
                  to track work output, verify weekly labour totals, and
                  improve social responsibility in the forestry sector.
                </p>
              </div>

            </div>
          </section>

          
          {/* === HOW DOES IT WORK SECTION === */}
          <section className="how-section container mb-5">
            <h2 className="section-title mb-4">How Does It Work?</h2>

            <div className="row g-4">

              {/* === CARD 1 === */}
              <div className="col-md-6 col-lg-3">
                <div className="how-card h-100">
                  <div className="how-icon-text">📱</div>
                  <h3 className="how-title">BrotherHood Mobile App</h3>
                  <p className="how-text">
                    Offline-first Android app for supervisors to record daily work as
                    usual. All captured data is synced to a central database.
                  </p>
                </div>
              </div>

              {/* === CARD 2 === */}
              <div className="col-md-6 col-lg-3">
                <div className="how-card h-100">
                  <div className="how-icon-text">🖥️</div>
                  <h3 className="how-title">BrotherHood Office</h3>
                  <p className="how-text">
                    A Windows desktop application for administrators to view, capture,
                    correct and approve daily and weekly labour records.
                  </p>
                </div>
              </div>

              {/* === CARD 3 === */}
              <div className="col-md-6 col-lg-3">
                <div className="how-card h-100">
                  <div className="how-icon-text">🌐</div>
                  <h3 className="how-title">BrotherHood Portal</h3>
                  <p className="how-text">
                    A worker portal that allows every registered employee to view up to
                    6 months of their personal records — newest first.
                  </p>

                  {/* === LOGIN LINK === */}
                  <Link to="/login" className="portal-login-link">
                    Log In →
                  </Link>
                </div>
              </div>

              {/* === CARD 4 === */}
              <div className="col-md-6 col-lg-3">
                <div className="how-card h-100">
                  <div className="how-icon-text">🖨️</div>
                  <h3 className="how-title">Print Media</h3>
                  <p className="how-text">
                    Each week, <strong>I, the developer</strong> prints reports for all
                    registered workers and hand-delivers them.
                    This delivery service alone justifies the
                    <strong> SZL 50 subscription</strong>.
                  </p>
                </div>
              </div>

            </div>
          </section>
          

          {/* === IS IT WORTH IT? SECTION === */}
          <section className="worth-section container mb-5">
            <h2 className="section-title mb-4">
              Is It Worth It? 🤔
            </h2>

            <div className="row g-4">

              {/* === CARD 1: PRINT MEDIA VALUE === */}
              <div className="col-md-6 col-lg-4">
                <div className="worth-card h-100">
                  <div className="worth-icon-text">🧾</div>
                  <h3 className="worth-title">Weekly Print Media</h3>
                  <p className="worth-text">
                    Every week, each worker receives a minimum of <strong>two printed
                      pages</strong> showing their detailed labour records.
                    A sample printout will be provided to demonstrate the value and
                    transparency the system brings.
                  </p>
                </div>
              </div>

              {/* === CARD 2: SYSTEM MAINTENANCE === */}
              <div className="col-md-6 col-lg-4">
                <div className="worth-card h-100">
                  <div className="worth-icon-text">🛠️</div>
                  <h3 className="worth-title">System Maintenance</h3>
                  <p className="worth-text">
                    The SZL 50 subscription supports the maintenance of the
                    <strong>database</strong>,
                    <strong>Android app</strong>,
                    <strong>desktop app</strong>,
                    <strong>web portal</strong>,
                    and all supporting infrastructure required to keep the ecosystem
                    running smoothly.
                  </p>
                </div>
              </div>

              {/* === CARD 3: INTELLIGENCE PLATFORM (SUPERSET) === */}
              <div className="col-md-6 col-lg-4">
                <div className="worth-card h-100">
                  <div className="worth-icon-text">📊</div>
                  <h3 className="worth-title">The Intelligence Platform</h3>
                  <p className="worth-text">
                    Powered by a Superset instance, the <strong>Intelligence Platform</strong>
                    provides deep business insights, operational analytics, and forecasting
                    based on the data collected by the Brotherhood and any external
                    datasource.
                    It has its own independent roadmap and commercial potential.
                    <br /><br />
              <button 
  className="worth-link" 
  style={{ background: "none", border: "none", padding: 0, color: "#0d6efd", cursor: "pointer" }}
  onClick={() => setShowVideoModal(true)}
>
  ▶️ Watch demo video
</button>
                    <br /><br />
                    <a
                      href="http://localhost:8088"
                      target="_blank"
                      rel="noopener noreferrer"
                      className="portal-login-link"
                    >
                      Log In →
                    </a>
                  </p>
                </div>
              </div>

            </div>


            {/* === FUTURE VISION === */}
            <div className="worth-future mt-5">
              <h3 className="worth-future-title">The Future of the Platform</h3>
              <p className="worth-future-text">
                In the near future, we aim to provide a complete <strong>stock
                  and inventory management system</strong> for the parent  company.
                If embraced, the operational costs of running the ecosystem will gradually
                shift away from casual workers — while they still retain all the benefits.
              </p>

              <p className="worth-future-text">
                Even if the parent company chooses not to expand further, the value the
                Brotherhood receives <strong>right now</strong> far outweighs the
                subscription fee.
                And we are working to further reduce print costs by offering weekly
                reports via <strong>SMS , Whatsapp</strong> and other low-cost channels.
              </p>
            </div>
          </section>
          {/* === WEEKLY REPORTS & COST-BENEFIT SECTION === */}
          <section className="reports-section container mb-5">
            <h2 className="section-title mb-4 text-center">
              Weekly Reports & Cost-Benefit Overview
            </h2>

            <div className="row g-4 justify-content-center">

              {/* === CARD 1: FRONT PAGE === */}
              <div className="col-md-6 col-lg-4">
                <div className="report-card h-100">
                  <div className="report-frame">
                    <img
                      src={reportFront}
                      alt="Front Page"
                      className="img-fluid report-image"
                    />
                  </div>
                  <h3 className="report-title text-center mt-3">Front Page</h3>
                  <p className="report-text text-center">
                    The weekly summary sheet every worker receives,
                    showing the stacks they have produced on each day.
                  </p>
                </div>
              </div>

              {/* === CARD 2: BACK PAGE === */}
              <div className="col-md-6 col-lg-4">
                <div className="report-card h-100">
                  <div className="report-frame">
                    <img
                      src={reportBack}
                      alt="Back Page"
                      className="img-fluid report-image"
                    />
                  </div>
                  <h3 className="report-title text-center mt-3">Back Page</h3>
                  <p className="report-text text-center">
                    The detailed breakdown of all the stack details  with clear  outline
                    of cubes  per stack.
                  </p>
                </div>
              </div>

           {/* === CARD 3: COST BENEFIT === */}
          <div className="col-md-6 col-lg-4">
            <div className="report-card h-100">
              <div className="report-frame">
                <img
                  src={costBenefit}
                  alt="Cost Benefit Analysis"
                  className="img-fluid report-image"
                />
              </div>

              <h3 className="report-title text-center mt-3">Cost-Benefit Analysis</h3>

              <p className="report-text text-center">
                A visual comparison showing how the SZL 50 subscription
                delivers financial fairness, operational clarity,
                and significant long-term savings.
              </p>
        <a href={costBenefit}
        download="cost-benefit.png"  // you can customize the filename dynamically if needed
        className="zoom-download"
      >
        💾 Download
      </a>
            </div>
          </div>

            </div>
          </section>
          {/* === TECHNOLOGY SOUNDNESS SECTION === */}
          <section className="tech-section container mb-5">
            <h2 className="section-title mb-4 text-center">
              Is the Technology Sound? 🔍
            </h2>

            {/* === TECHNOLOGY IMAGES === */}
            <div className="row g-4 justify-content-center mb-4">
    
              {/* ANDROID APP IMAGE */}
              <div className="col-md-6 col-lg-5">
                <div className="tech-card h-100">
                  <div className="tech-frame">
                    <img
                      src={androidWorkFlow}
                      alt="Android App Repository"
                      className="img-fluid tech-image"
                    />
                  </div>
                  <h3 className="tech-title text-center mt-3">Android App</h3>
                  <p className="tech-text text-center">
                    Built with <strong>Kotlin + Jetpack Compose</strong>,
                    following an offline-first model with clean architecture.
                    automated releases using free git  functionalities + considerate migration strategies.
                  </p>
                </div>
              </div>

              {/* DESKTOP / COMPOSE APP IMAGE */}
              <div className="col-md-6 col-lg-5">
                <div className="tech-card h-100">
                  <div className="tech-frame">
                    <img
                      src={desktopWorkFlow}
                      alt="Compose Desktop App Repository"
                      className="img-fluid tech-image"
                    />
                  </div>
                  <h3 className="tech-title text-center mt-3">Desktop App</h3>
                  <p className="tech-text text-center">
                    Developed using <strong>Kotlin + Jetpack Compose Desktop</strong>,
                    giving administrators a fast, native, and modern workflow.
                    automated releases using free git  functionalities + considerate migration strategies.
                  </p>
                </div>
              </div>

            </div>

            {/* === TECHNOLOGY EXPLANATIONS IN INFO CARDS === */}
            <div className="row g-4">

              {/* CARD 1 — UNIFIED LANGUAGE */}
              <div className="col-md-6 col-lg-4">
                <div className="tech-info-card h-100">
                  <div className="tech-info-icon">💻</div>
                  <h3 className="tech-info-title">One Language. One Philosophy.</h3>
                  <p className="tech-info-text">
                    The entire ecosystem — Android app, Ktor server, and
                    the Desktop application — is powered by <strong>Kotlin</strong>.
                    I have worked with Kotlin for <strong>over 3 years</strong>,
                    meaning long-term stability, familiarity, and fast delivery.
                  </p>
                </div>
              </div>

              {/* CARD 2 — SIMPLE REST ARCHITECTURE */}
              <div className="col-md-6 col-lg-4">
                <div className="tech-info-card h-100">
                  <div className="tech-info-icon">🔗</div>
                  <h3 className="tech-info-title">Simple REST Architecture</h3>
                  <p className="tech-info-text">
                    At the core is a clean, well-structured
                    <strong>REST API built with Ktor</strong>.
                    No unnecessary complexity — just stable endpoints
                    connecting apps, databases, and reporting tools seamlessly.
                  </p>
                </div>
              </div>

              {/* CARD 3 — MODERN FRAMEWORKS */}
              <div className="col-md-6 col-lg-4">
                <div className="tech-info-card h-100">
                  <div className="tech-info-icon">🧩</div>
                  <h3 className="tech-info-title">Future-Proof Frameworks</h3>
                  <p className="tech-info-text">
                    Jetpack Compose, Material Design, and Kotlin Multiplatform (KMP)
                    are <strong>Google-supported, fast-growing DSL frameworks</strong>
                    with massive community backing — ensuring reliability into the future.
                  </p>
                </div>
              </div>

              {/* CARD 4 — DOCKER ARCHITECTURE */}
              <div className="col-md-6 col-lg-4">
                <div className="tech-info-card h-100">
                  <div className="tech-info-icon">🐳</div>
                  <h3 className="tech-info-title">Resilient Deployment With Docker</h3>
                  <p className="tech-info-text">
                    Superset, the Ktor server, and the full-stack portal run in
                    <strong>Docker containers</strong>. This guarantees isolation,
                    resilience, fast recovery, and easy upgrades without downtime.
                  </p>
                </div>
              </div>

              {/* CARD 5 — OPEN TECHNOLOGY */}
              <div className="col-md-6 col-lg-4">
                <div className="tech-info-card h-100">
                  <div className="tech-info-icon">📚</div>
                  <h3 className="tech-info-title">Open, Documented, Transparent</h3>
                  <p className="tech-info-text">
                    Nothing in this platform is hidden.
                    <strong>PostgreSQL</strong> is a world-class relational DB.
                    <strong>Apache Superset</strong> is free, open-source, and well-documented.
                    Anyone can inspect and verify how the system works.
                  </p>
                </div>
              </div>

              {/* CARD 6 — COMMITMENT */}
              <div className="col-md-6 col-lg-4">
                <div className="tech-info-card h-100">
                  <div className="tech-info-icon">❤️</div>
                  <h3 className="tech-info-title">Commitment to My Community</h3>
                  <p className="tech-info-text">
                    This is my home.
                    I am not outsourcing this system or abandoning it.
                    If you give me the chance, I will continue to maintain and grow
                    this technology for the people who depend on it.
                  </p>
                </div>
              </div>

            </div>
          </section>

{/* === GALLERY CTA SECTION === */}
<section className="gallery-cta container mb-5">
  <div 
    className="p-5 text-center rounded-4 shadow-lg"
    style={{
      background: "#0d6efd",
      color: "white",
      border: "3px solid #084298"
    }}
  >
    <h2 className="mb-3" style={{ fontWeight: "700" }}>
      📸 Explore the Brotherhood Gallery
    </h2>

    <p className="mb-4" style={{ fontSize: "1.2rem" }}>
      View real screenshots, workflows, mobile app screens, desktop UI samples,
      and the entire system in action.
      <br />
      A transparent look into how everything actually works.
    </p>

    <Link
      to="/gallery"
      className="btn btn-light btn-lg px-5 py-3"
      style={{
        fontWeight: "600",
        borderRadius: "12px",
        fontSize: "1.3rem"
      }}
    >
      Visit Gallery →
    </Link>
  </div>
          </section>
        

          {showVideoModal && (
  <div 
    className="video-modal-overlay" 
    style={{ 
      position: "fixed",
      inset: 0,
      background: "rgba(0,0,0,0.6)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      zIndex: 99999
    }}
  >
<div 
  className="video-modal-content" 
  style={{
    width: "90%",
    maxWidth: "900px",
    background: "white",
    padding: "40px",
    borderRadius: "12px",
    position: "relative",
    zIndex: 10000,            
    boxShadow: "0 0 20px rgba(0,0,0,0.3)"
  }}
>
  <button 
  onClick={() => setShowVideoModal(false)}
  style={{
    position: "absolute",
    top: "2px",
    right: "12px",
    background: "transparent",
    border: "none",
    fontSize: "1.8rem",
    cursor: "pointer",
    zIndex: 20000,  
  }}
>
  ✖
</button>

      {/* EMBEDDED VIDEO */}
      <div className="ratio ratio-16x9">
        <iframe
          width="100%"
          height="480"
          src="https://www.youtube.com/embed/WUQOw7gmUNY?si=2ZWiAg3qa8HUNFrJ"
          title="Intelligence Platform Demo"
          frameBorder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowFullScreen
          style={{
            borderRadius: "12px",
            width: "100%",
            height: "480px"
          }}
        ></iframe>
      </div>
    </div>
  </div>
)}

   
          {/* === FOOTER === */}
          <Footer />
        </div>)}
        </div>
      
  
  );
}