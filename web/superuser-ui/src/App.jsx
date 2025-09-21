import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./Routes";
import './App.css'

function App() {
  return (
    <BrowserRouter basename="">
      <AppRoutes />
    </BrowserRouter>
  );
}

export default App;