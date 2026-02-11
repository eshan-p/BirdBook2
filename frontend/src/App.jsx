import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Feed from "./pages/Feed";
import Landing from "./pages/Landing";
import Header from "./components/layout/Header";
import Sighting from "./pages/Sighting";
import Profile from "./pages/Profile";
import Birds from "./pages/Birds"; 
import Groups from "./pages/Groups";
import Friends from "./pages/Friends";
import Users from "./pages/Users";
import GroupFeed from "./pages/GroupFeed";
import SearchResults from "./pages/SearchResults";
import { AuthProvider } from "./context/AuthContext";
import OtherProfile from "./pages/OtherProfile";
import BirdDetail from "./pages/BirdDetail";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/layout/Layout"
import Onboarding from "./pages/Onboarding";

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/onboarding" element={<ProtectedRoute><Onboarding /></ProtectedRoute>} />
          <Route element={<Layout />}>
            <Route path="/feed" element={<Feed />} />
            <Route path="/birds" element={<Birds />} /> 
            <Route path="/birds/:birdId" element={<BirdDetail />} />
            <Route path="/sightings/:postId" element={<Sighting />} />
            <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
            <Route path="/friends" element={<ProtectedRoute><Friends /></ProtectedRoute>} />
            <Route path="/users" element={<ProtectedRoute><Users /></ProtectedRoute>} />
            <Route path="/groups" element={<ProtectedRoute><Groups /></ProtectedRoute>} />
            <Route path="/groups/:groupId" element={<ProtectedRoute><GroupFeed /></ProtectedRoute>} />
            <Route path="/user/:userId" element={<ProtectedRoute><OtherProfile /></ProtectedRoute>} />
            <Route path="/search" element={<ProtectedRoute><SearchResults /></ProtectedRoute>} />
          </Route>
        </Routes>
      </Router>
    </AuthProvider>
  );
}