import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../api/api';
import '../styles/LoginPage.css'; // Reuse styles

const EditProfilePage = () => {
    const { user, login, logout, loading } = useContext(AuthContext);
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        nickname: '',
        email: '',
        pw: '',
        pwConfirm: '',
    });
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        // AuthContext의 로딩이 끝나고, user 상태가 확정되었을 때만 로직 수행
        if (!loading) {
            if (user) {
                setFormData({
                    nickname: user.nickname || '',
                    email: user.email || '',
                    pw: '',
                    pwConfirm: '',
                });
            } else {
                // 로딩이 끝났는데 user가 없으면 로그인 페이지로 이동
                navigate('/login');
            }
        }
    }, [user, loading, navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    };

    const validateForm = () => {
        const newErrors = {};
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!formData.nickname) newErrors.nickname = '닉네임을 입력해주세요';
        if (!formData.email) newErrors.email = '이메일을 입력해주세요';
        else if (!emailRegex.test(formData.email)) newErrors.email = '올바른 이메일 형식이 아닙니다';

        // Password is optional, but if filled, it must be valid
        if (formData.pw && formData.pw.length < 6) {
            newErrors.pw = '비밀번호는 6자리 이상이어야 합니다';
        }
        if (formData.pw !== formData.pwConfirm) {
            newErrors.pwConfirm = '비밀번호가 일치하지 않습니다';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleUpdate = async (e) => {
        console.log('handleUpdate called');
        e.preventDefault();
        if (!validateForm()) return;
        setIsLoading(true);

        const payload = {
            nickname: formData.nickname,
            email: formData.email,
        };

        if (formData.pw) {
            payload.pw = formData.pw;
        }

        try {
            const res = await api.put(`/member/${user.id}`, payload);

            if (res.data.success) {
                alert('회원 정보가 수정되었습니다.');
                // Update user context with new info
                login({ ...user, nickname: formData.nickname, email: formData.email });
                navigate('/');
            } else {
                alert('수정 실패: ' + res.data.message);
            }
        } catch (error) {
            console.error('Error updating profile:', error);
            if (error.response) {
                console.error('Backend response:', error.response.data);
                alert('프로필 수정 실패: ' + (error.response.data.message || '서버에서 구체적인 오류 메시지를 보내지 않았습니다.'));
            } else {
                alert('프로필 수정 중 오류가 발생했습니다.');
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleDelete = async () => {
        if (window.confirm('정말로 회원 탈퇴를 하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
            setIsLoading(true);
            try {
                const res = await api.delete(`/member/${user.id}`);
                if (res.data.success) {
                    alert('회원 탈퇴가 완료되었습니다.');
                    logout();
                    navigate('/');
                } else {
                    alert('회원 탈퇴 실패: ' + res.data.message);
                }
            } catch (error) {
                console.error('Error deleting account:', error);
                alert('회원 탈퇴 중 오류가 발생했습니다.');
            } finally {
                setIsLoading(false);
            }
        }
    };

    if (!user) {
        return null; // Or a loading spinner
    }

    return (
        <div className="login-container">
            <div className="login-card">
                <h1>회원정보 수정</h1>
                <form onSubmit={handleUpdate} className="login-form">
                    <div>
                        <label>아이디</label>
                        <input value={user.id} readOnly disabled />
                    </div>
                    <div>
                        <label>닉네임</label>
                        <input
                            name="nickname"
                            placeholder="닉네임"
                            value={formData.nickname}
                            onChange={handleChange}
                            disabled={isLoading}
                            className={errors.nickname ? 'error' : ''}
                        />
                        {errors.nickname && <div className="error-text">{errors.nickname}</div>}
                    </div>
                    <div>
                        <label>이메일</label>
                        <input
                            name="email"
                            placeholder="이메일"
                            value={formData.email}
                            onChange={handleChange}
                            disabled={isLoading}
                            className={errors.email ? 'error' : ''}
                        />
                        {errors.email && <div className="error-text">{errors.email}</div>}
                    </div>
                    <div>
                        <label>새 비밀번호 (변경 시에만 입력)</label>
                        <input
                            name="pw"
                            type="password"
                            placeholder="새 비밀번호"
                            value={formData.pw}
                            onChange={handleChange}
                            disabled={isLoading}
                            className={errors.pw ? 'error' : ''}
                        />
                        {errors.pw && <div className="error-text">{errors.pw}</div>}
                    </div>
                    <div>
                        <label>새 비밀번호 확인</label>
                        <input
                            name="pwConfirm"
                            type="password"
                            placeholder="새 비밀번호 확인"
                            value={formData.pwConfirm}
                            onChange={handleChange}
                            disabled={isLoading}
                            className={errors.pwConfirm ? 'error' : ''}
                        />
                        {errors.pwConfirm && <div className="error-text">{errors.pwConfirm}</div>}
                    </div>
                    <button type="submit" disabled={isLoading}>
                        {isLoading ? '수정 중...' : '정보 수정'}
                    </button>
                </form>

                <button onClick={() => navigate('/')} disabled={isLoading} style={{ marginTop: '10px' }}>
                    취소
                </button>

                <hr style={{ width: '100%', margin: '20px 0' }} />

                <button onClick={handleDelete} disabled={isLoading} className="delete-button">
                    회원 탈퇴
                </button>
            </div>
        </div>
    );

};

export default EditProfilePage;

