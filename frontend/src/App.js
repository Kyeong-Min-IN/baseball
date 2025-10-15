import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';

import Header from './components/Header';
import HomePage from './pages/HomePage';
import KboPage from './pages/KboPage';
import KboBoard from './pages/KboBoard';
import KboBoardDetail from './pages/KboBoardDetail';
import LoginPage from './pages/loginpage.js';
import ProfilePage from './pages/ProfilePage';
import PostForm2 from './pages/PostForm2';
import PostFormWithComments from './pages/PostFormWithComments';
import TeamSetup from './pages/TeamSetup';
import TeamInfoPage from './pages/TeamInfoPage';
import TeamSetupPage from './pages/TeamSetupPage';
import MainPage from './pages/MainPage';
import GamePage from './pages/GamePage';
import ResultPage from './pages/ResultPage';
import HelpPage from './pages/HelpPage';
import EditProfilePage from './pages/EditProfilePage';

import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Header /> {/* 로그인 상태 자동 연동 */}
        <div className="App">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/ProfilePage" element={<ProfilePage />} />
            <Route path="/kbo" element={<KboPage />} />
            <Route path="/kboBoard" element={<KboBoard />} />
            <Route path="/kboBoard/:id" element={<KboBoardDetail />} />
            <Route path="/PostFormWithComments/new" element={<PostFormWithComments />} />
            <Route path="/PostFormWithComments/edit/:id" element={<PostFormWithComments />} />
            <Route path="/PostForm2/edit/:id" element={<PostForm2 />} />
            <Route path="/game/setup" element={<TeamSetupPage />} />
            <Route path="/game/game" element={<MainPage />} />
            <Route path="/game/TeamSetup" element={<TeamSetup />} />
            <Route path="/game/play" element={<GamePage />} />
            <Route path="/game/result" element={<ResultPage />} />
            <Route path="/team/info" element={<TeamInfoPage />} />
            <Route path="/help" element={<HelpPage />} />
            <Route path="/edit-profile" element={<EditProfilePage />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
