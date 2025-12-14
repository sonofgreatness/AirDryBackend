import Footer from "../../layouts/tpl/Footer";
import { AuthService } from "../../services/AuthService"
import { useEffect } from "react"

export default function Gallery() {
useEffect(() => {
        AuthService.greetServerFromGallery();
    }, []);

  const SECTIONS = [
    {
      id: "mobile",
      title: "BrotherHood Mobile (Android)",
      description: {
        audience: "Supervisors and mobile field workers.",
        access: "App login with assigned username & password.",
        version: "v1.0.0 (Latest)",
        offline: "Yes — fully offline-first with automatic syncing.",
      },
      videos: [
        "https://www.youtube.com/embed/EqFKty84KRM?si=hVVY4nAYAJsRYXSr"
      ],
    },

    {
      id: "office",
      title: "BrotherHood Office (Windows Desktop)",
      description: {
        audience: "Administrators, officers & supervisors.",
        access: "Desktop login with administrative permissions.",
        version: "v1.0.0 (Latest)",
        offline: "Partial — requires periodic server connection.",
      },
        videos: [
          "https://www.youtube.com/embed/zywGLc8KZX0?si=RwWMh7duPSw06deF",
        "https://www.youtube.com/embed/n7WM85mtOvE?si=pN99MSQWEElR4Vac" ,
         ],
    },

    {
      id: "portal",
      title: "BrotherHood Portal (Web)",
      description: {
        audience: "All registered workers.",
        access: "username + password",
        version: "Live",
        offline: "No — requires internet.",
      },
      videos: [
       "https://www.youtube.com/embed/RCa4XKLJlTE?si=njKs0F_PxsEHOwDR",
    
      ],
    },
  ];

  return (
    <div className="page-pad gallery-page">
      <div className="container py-5">

        <h1 className="gallery-main-title text-center mb-5">
          Brotherhood Platform – Visual Gallery
        </h1>

        {SECTIONS.map((s, i) => (
          <div className="gallery-section mb-5" key={i}>
            
            {/* === SECTION TITLE WITH UNDERLINE === */}
            <div className="gallery-title-wrapper mb-3">
              <h2 className="gallery-section-title">{s.title}</h2>
              <div className="gallery-title-underline"></div>
            </div>

            {/* === DESCRIPTION CARD === */}
            <div className="gallery-info-card mb-4">
              <p><strong>Intended Audience:</strong> {s.description.audience}</p>
              <p><strong>User Account Access Needed:</strong> {s.description.access}</p>
              <p><strong>Latest Version:</strong> {s.description.version}</p>
              <p><strong>Offline-Capable:</strong> {s.description.offline}</p>
            </div>

            {/* === VIDEOS === */}
            <div className="row g-4">
              {s.videos.map((url, idx) => (
                <div className="col-md-6" key={idx}>
                  <div className="video-frame">
                    <iframe
                      src={url}
                      title="BrotherHood Demo Video"
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                      allowFullScreen
                    />
                  </div>
                </div>
              ))}
            </div>

          </div>
        ))}

      </div>

      <Footer />
    </div>
  );
}