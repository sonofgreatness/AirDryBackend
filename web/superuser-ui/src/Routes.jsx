import { Routes, Route } from "react-router-dom";
import DefaultLayout from "./layouts/Default";
import NewDefaultLayout from "./layouts/tpl/NewDefault";
import Organization from "./pages/Organizations";

import Users from "./pages/Users";
import UserForm from "./pages/User/UserForm";
import Login from "./layouts/Login";
import Profile from "./pages/User/Profile";
import LandingPage from "./pages/Info/LandingPage";
import UserStats from "./pages/User/UserStats"; 
import StacksLibrary from "./pages/Stacks/StacksLibrary";
import Gallery from "./pages/Gallery/Gallery";
import Manifesto from "./pages/Info/Manifesto";
import Realeases from "./pages/Release/Releases"; 
import AdminReleases from "./pages/Release/AdminReleases";
import ReleaseForm from "./pages/Release/ReleaseForm";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/manifesto" element={<Manifesto />} />
      <Route path="/releases" element={<Realeases />} />
      <Route path="/releases/all" element={<AdminReleases />} />
      <Route path="/releases/create" element={<ReleaseForm/> }/>
      <Route path="/releases/:id/edit" element={<ReleaseForm/> }/>
      <Route path="/" element={<LandingPage />} />
      <Route path="/gallery" element={ <Gallery/>} />
      <Route path="/users" element={<DefaultLayout><Users /></DefaultLayout>} />
      <Route path="/users/create" element={<DefaultLayout><UserForm /></DefaultLayout>} />
      <Route path="/users/:id/edit" element={<DefaultLayout><UserForm /></DefaultLayout>} />
      <Route path="/organizations" element={<DefaultLayout><Organization /></DefaultLayout>} />
      <Route path="/profile" element={<NewDefaultLayout><Profile /></NewDefaultLayout>} />
       <Route path="/user_stats" element={<NewDefaultLayout><UserStats /></NewDefaultLayout>} />
      <Route path="/stacks_library" element={<NewDefaultLayout><StacksLibrary/></NewDefaultLayout> } />
    </Routes>
  );
}
