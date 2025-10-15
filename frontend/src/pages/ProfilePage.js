import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/LoginPage.css';

const RegisterPage = ({ setUserId }) => {
  const [formData, setFormData] = useState({ id: '', nickname: '', pw: '', pwConfirm: '', email: '' });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validateForm = () => {
    const newErrors = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!formData.id) newErrors.id = '아이디를 입력해주세요';
    if (!formData.nickname) newErrors.nickname = '닉네임을 입력해주세요';
    if (!formData.pw) newErrors.pw = '비밀번호를 입력해주세요';
    else if (formData.pw.length < 6) newErrors.pw = '비밀번호는 6자리 이상이어야 합니다';
    if (formData.pw !== formData.pwConfirm) newErrors.pwConfirm = '비밀번호가 일치하지 않습니다';
    if (!formData.email) newErrors.email = '이메일을 입력해주세요';
    else if (!emailRegex.test(formData.email)) newErrors.email = '올바른 이메일 형식이 아닙니다';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;
    setIsLoading(true);

    try {
      // 회원가입
      const registerRes = await axios.post('http://localhost:8080/api/login/register', {
        id: formData.id,
        pw: formData.pw,
        nickname: formData.nickname,
        email: formData.email
      });

      if (!registerRes.data.success) {
        setErrors(prev => ({ ...prev, id: registerRes.data.message }));
        setIsLoading(false);
        return;
      }

      alert('회원가입 완료.');

      // 폼 초기화
      setFormData({ id: '', nickname: '', pw: '', pwConfirm: '', email: '' });
      setErrors({});

      // Redirect to login page
      navigate('/login'); // Redirect to login page

    } catch (error) {
      console.error(error);
      alert('회원가입 중 오류가 발생했습니다.'); // More appropriate error message
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>회원가입</h1>
          <p>새로운 계정을 만들어보세요</p>
        </div>
        
        <form onSubmit={handleRegister} className="login-form">
          <div className="login-form-group">
            <input
              name="id"
              placeholder="아이디"
              value={formData.id}
              onChange={handleChange}
              disabled={isLoading}
              className={`login-form-input ${errors.id ? 'error' : ''}`}
            />
            {errors.id && <div className="error-text">{errors.id}</div>}
          </div>

          <div className="login-form-group">
            <input
              name="nickname"
              placeholder="닉네임"
              value={formData.nickname}
              onChange={handleChange}
              disabled={isLoading}
              className={`login-form-input ${errors.nickname ? 'error' : ''}`}
            />
            {errors.nickname && <div className="error-text">{errors.nickname}</div>}
          </div>

          <div className="login-form-group">
            <input
              name="pw"
              type="password"
              placeholder="비밀번호"
              value={formData.pw}
              onChange={handleChange}
              disabled={isLoading}
              className={`login-form-input ${errors.pw ? 'error' : ''}`}
            />
            {errors.pw && <div className="error-text">{errors.pw}</div>}
          </div>

          <div className="login-form-group">
            <input
              name="pwConfirm"
              type="password"
              placeholder="비밀번호 확인"
              value={formData.pwConfirm}
              onChange={handleChange}
              disabled={isLoading}
              className={`login-form-input ${errors.pwConfirm ? 'error' : ''}`}
            />
            {errors.pwConfirm && <div className="error-text">{errors.pwConfirm}</div>}
          </div>

          <div className="login-form-group">
            <input
              name="email"
              placeholder="이메일"
              value={formData.email}
              onChange={handleChange}
              disabled={isLoading}
              className={`login-form-input ${errors.email ? 'error' : ''}`}
            />
            {errors.email && <div className="error-text">{errors.email}</div>}
          </div>

          <button type="submit" disabled={isLoading} className="login-form-button login-form-button-primary">
            {isLoading ? (
              <>
                <span className="login-loading-spinner"></span>
                회원가입 중...
              </>
            ) : (
              '회원가입'
            )}
          </button>
        </form>

        <div className="login-form-footer">
          <p className="login-form-footer-text">이미 계정이 있으신가요?</p>
          <button 
            onClick={() => navigate('/login')} 
            disabled={isLoading}
            className="login-form-button login-form-button-secondary"
          >
            로그인으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
