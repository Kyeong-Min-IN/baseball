import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../api/api'; // api 인스턴스 임포트
import '../styles/LoginPage.css';

const LoginPage = () => {
    const [id, setId] = useState('');
    const [pw, setPw] = useState('');
    const [email, setEmail] = useState('');
    const [nickname, setNickname] = useState('');
    const [isRegistering, setIsRegistering] = useState(false);
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            // Spring Security가 기대하는 form-urlencoded 형태로 데이터 전송
            const params = new URLSearchParams();
            params.append('username', id); // 'id'를 'username'으로 변경
            params.append('password', pw); // 'pw'를 'password'로 변경

            const res = await api.post('/login', params, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });

            if (res.data.success) {
                login(res.data.userInfo); // <-- userInfo가 제대로 전달되는지 확인
                navigate('/');
            } else {
                alert(res.data.message);
            }
        } catch (error) {
            console.error("Login error:", error);
            alert("로그인 중 오류가 발생했습니다.");
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            const res = await api.post('/login/register', { id, pw, email, nickname });
            if (res.data.success) {
                alert("회원가입 성공! 로그인 해주세요.");
                setIsRegistering(false);
                setId('');
                setPw('');
                setEmail('');
                setNickname('');
            } else {
                alert(res.data.message);
            }
        } catch (error) {
            console.error("Register error:", error);
            alert("회원가입 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h1>{isRegistering ? '회원가입' : '로그인'}</h1>
                <form onSubmit={isRegistering ? handleRegister : handleLogin} className="login-form">
                    <input
                        type="text"
                        placeholder="아이디"
                        value={id}
                        onChange={(e) => setId(e.target.value)}
                        required
                    />
                    <input
                        type="password"
                        placeholder="비밀번호"
                        value={pw}
                        onChange={(e) => setPw(e.target.value)}
                        required
                    />
                    {isRegistering && (
                        <>
                            <input
                                type="email"
                                placeholder="이메일"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                            <input
                                type="text"
                                placeholder="닉네임"
                                value={nickname}
                                onChange={(e) => setNickname(e.target.value)}
                                required
                            />
                        </>
                    )}
                    <button type="submit">{isRegistering ? '회원가입' : '로그인'}</button>
                </form>
                <button onClick={() => setIsRegistering(!isRegistering)} className="toggle-button">
                    {isRegistering ? '로그인 페이지로' : '회원가입하기'}
                </button>
            </div>
        </div>
    );
};

export default LoginPage;