import React, { createContext, useState, useEffect } from "react";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true); // 로딩 상태 추가

  useEffect(() => {
    try {
      const storedUser = localStorage.getItem('user');
      if (storedUser && storedUser !== "undefined") {
        setUser(JSON.parse(storedUser));
      } 
    } catch (e) {
      console.error("Failed to parse stored user from localStorage:", e);
      localStorage.removeItem('user');
    } finally {
      // localStorage 확인 작업이 끝나면 로딩 상태를 false로 변경
      setLoading(false);
    }
  }, []);

  const login = (userInfo) => {
    setUser(userInfo);
    localStorage.setItem('user', JSON.stringify(userInfo));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('accessToken'); 
  };

  // 로딩 중이 아닐 때만 children을 렌더링
  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
