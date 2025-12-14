import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import Footer from "../../layouts/tpl/Footer";
import { AuthService } from "../../services/AuthService";
import { ReleaseService } from "../../services/ReleaseService";
import { type ReleaseDTO } from "../../util/types/common";
import wind_1 from "../../assets/icons/wind_1.png"
import wind_2 from "../../assets/icons/wind_2.png"
import wind_3 from "../../assets/icons/wind_3.png"
import and_1  from "../../assets/icons/and_1.png"
import and_2 from "../../assets/icons/and_2.png"
import and_3  from "../../assets/icons/and_3.png"
/* =========================
   REUSABLE COMPONENTS
========================= */

function ReleaseCarousel({ images }: { images: string[] }) {
  const [index, setIndex] = useState(0);

  return (
    <div className="release-carousel">
      <img src={images[index]} alt="Release preview" />

      <div className="carousel-controls">
        {images.map((_, i) => (
          <button
            key={i}
            className={i === index ? "active" : ""}
            onClick={() => setIndex(i)}
            aria-label={`Preview ${i + 1}`}
          />
        ))}
      </div>
    </div>
  );
}

function ReleaseNotes({ platform }: { platform: "Windows" | "Android" }) {
  return (
    <div className="release-notes">
      <h4>Installation Notes</h4>

      {platform === "Windows" && (
        <ul>
          <li>Supported OS: Windows 10 (64-bit) and later</li>
          <li>Installer requires local administrator privileges</li>
          <li>Logs found at (user.home)/AppData/Local/BrotherHoodOffice/logs/</li>
          <li>Initial set up need active server connections</li>
        </ul>
      )}

      {platform === "Android" && (
        <ul>
          <li>Supported OS: Android 9.0 (API 28) and later</li>
          <li>APK installation requires “Install unknown apps” enabled</li>
          <li>Optimized for low-memory and low-connectivity environments</li>
          <li>Offline mode supported after first launch</li>
        </ul>
      )}
    </div>
  );
}

function ReleaseTable({
  releases,
  emptyLabel,
}: {
  releases: ReleaseDTO[];
  emptyLabel: string;
}) {
  return (
    <table className="user-table">
      <thead>
        <tr>
          <th>Platform</th>
          <th>Download</th>
          <th>Release Date</th>
        </tr>
      </thead>
      <tbody>
        {releases.length === 0 ? (
          <tr>
            <td colSpan={3} className="no-users">
              {emptyLabel}
            </td>
          </tr>
        ) : (
          releases.map((release) => (
            <tr key={release.id}>
              <td>{release.platform}</td>
              <td>
                <a
                  href={release.link}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Download
                </a>
              </td>
              <td>{release.dateOfRelease}</td>
            </tr>
          ))
        )}
      </tbody>
    </table>
  );
}

/* =========================
   MAIN PAGE
========================= */

export default function Releases() {
  const [windows, setWindows] = useState<ReleaseDTO[]>([]);
  const [android, setAndroid] = useState<ReleaseDTO[]>([]);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  async function loadData() {
    try {
      const [win, and] = await Promise.all([
        ReleaseService.loadWindowsReleases(),
        ReleaseService.loadAndroidReleases(),
      ]);
      setWindows(win);
      setAndroid(and);
    } catch {
      setError("Failed to load release information.");
    }
  }

  useEffect(() => {
    AuthService.greetServerFromReleases();
    loadData();
  }, []);

  if (error) {
    return (
      <div className="user-form-page">
        <div className="user-form-card text-red-600">
          <p>{error}</p>
          <button onClick={() => navigate("/")} className="ticket-btn mt-4">
            Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-pad gallery-page">
      <div className="container py-5">

        <h1 className="gallery-main-title text-center mb-5">
          Software Releases
        </h1>

        {/* ================= WINDOWS ================= */}
        <section className="release-section">
          <h2 className="release-title">Windows</h2>

          <ReleaseCarousel
            images={[
              wind_2,
              wind_1,
              wind_3,
            ]}
          />

          <ReleaseNotes platform="Windows" />

          <ReleaseTable
            releases={windows}
            emptyLabel="No Windows releases found"
          />
        </section>

        {/* ================= ANDROID ================= */}
        <section className="release-section">
          <h2 className="release-title">Android</h2>

          <ReleaseCarousel
            images={[
             and_1,
             and_2,
            and_3,
            ]}
          />

          <ReleaseNotes platform="Android" />

          <ReleaseTable
            releases={android}
            emptyLabel="No Android releases found"
          />
        </section>

      </div>

      <Footer />
    </div>
  );
}
