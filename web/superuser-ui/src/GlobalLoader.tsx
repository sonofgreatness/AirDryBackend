
import './global.css'; // custom CSS for spinner
export default function GlobalLoader() {
  return (
    <div className="global-loader-overlay">
      <div className="ncaloader"></div>
    </div>
  );
}