import { StrictMode, useState, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import 'bootstrap/dist/css/bootstrap.min.css';
import './index.css';
import App from './App';
import GlobalLoader from './GlobalLoader'; // create this

const LoaderWrapper = () => {
  const [appReady, setAppReady] = useState(false);

  useEffect(() => {
    // wait for the next tick so React can bootstrap
    const timer = setTimeout(() => setAppReady(true), 50);
    return () => clearTimeout(timer);
  }, []);

  return (
    <>
      {!appReady && <GlobalLoader />}
      {appReady && <App />}
    </>
  );
};

const root = document.getElementById('root');
if (root) {
  createRoot(root).render(
    <StrictMode>
      <LoaderWrapper />
    </StrictMode>
  );
}