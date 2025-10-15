import React, { useState, useEffect, useContext, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import api from '../api/api';
import '../styles/kboboard.css';

const categoryTextMap = { 1:"자유",2:"KBO",3:"NPB",4:"MLB",5:"사회인야구" };

const KboBoardDetail = () => {
  const { user } = useContext(AuthContext);
  const { id } = useParams(); // 게시글 번호
  const navigate = useNavigate();

  const [board, setBoard] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingCommentText, setEditingCommentText] = useState('');
  const [loading, setLoading] = useState(true);
  const [showDropdown, setShowDropdown] = useState(false); // 더보기 메뉴 상태
  const [prevCommentsLength, setPrevCommentsLength] = useState(0); // 이전 댓글 수 추적

  const commentsEndRef = useRef(null);
  const dropdownRef = useRef(null);

  const scrollToBottom = () => {
    commentsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // 더보기 메뉴 외부 클릭 감지
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // 게시글 + 댓글 불러오기
  const fetchBoardDetail = async () => {
    try {
      const res = await api.get(`/board/${id}`);
      setBoard(res.data.board);
      const newComments = res.data.comments || [];
      setComments(newComments);
      return newComments.length;
    } catch (err) {
      console.error(err);
      alert('게시글 로딩 실패');
      return 0;
    }
  };

  useEffect(() => {
    setLoading(true);
    fetchBoardDetail().then(commentsLength => {
      setPrevCommentsLength(commentsLength);
      setLoading(false);
    });

    const interval = setInterval(async () => {
      const commentsLength = await fetchBoardDetail();
      // 댓글 수가 증가했을 때만 스크롤
      if (commentsLength > prevCommentsLength) {
        setPrevCommentsLength(commentsLength);
        setTimeout(scrollToBottom, 100); // 약간의 지연 후 스크롤
      }
    }, 5000); // 5초마다 댓글 갱신

    return () => clearInterval(interval);
  }, [id]);

  // 댓글이 새로 작성되었을 때만 스크롤 (댓글 수가 증가한 경우)
  useEffect(() => {
    if (comments.length > prevCommentsLength && prevCommentsLength > 0) {
      scrollToBottom();
    }
  }, [comments.length, prevCommentsLength]);

  // 댓글 작성
  const handleCommentSubmit = async () => {
    if (!user) {
      alert('로그인 후 작성 가능합니다.');
      return;
    }
    if (!newComment.trim()) return;

    try {
      await api.post(`/comment`, {
        boardNo: id,
        writer: user.id, // Changed user.Id to user.id
        text: newComment.trim(),
      }, { headers: { 'Content-Type': 'application/json' } });

      setNewComment('');
      const newCommentsLength = await fetchBoardDetail(); // 댓글 목록 갱신
      setPrevCommentsLength(newCommentsLength);
      setTimeout(scrollToBottom, 100); // 새 댓글 작성 시 스크롤
    } catch (err) {
      console.error(err);
      alert('댓글 작성 실패');
    }
  };

  // 댓글 삭제
  const handleCommentDelete = async (commentId, commentWriter) => {
    if (!user || user.id !== commentWriter) { // Changed user.Id to user.id
      alert('자신의 댓글만 삭제 가능합니다.');
      return;
    }
    if (!window.confirm('댓글을 삭제하시겠습니까?')) return;

    try {
      await api.delete(`/comment/${id}/${commentId}`);
      setComments(prev => prev.filter(c => c.commentId !== commentId));
    } catch (err) {
      console.error(err);
      alert('댓글 삭제 실패');
    }
  };

  // 게시글 삭제
  const handleDeletePost = async () => {
    if (!user || user.id !== board.writer) {
      alert('본인 글만 삭제 가능합니다.');
      return;
    }
    if (!window.confirm('게시글을 삭제하시겠습니까?')) return;

    try {
      await api.delete(`/board/${id}`, { params: { writer: user.id } });
      alert('게시글이 삭제되었습니다.');
      navigate('/kboBoard');
    } catch (err) {
      console.error(err);
      alert('게시글 삭제 실패');
    }
    setShowDropdown(false); // 드롭다운 닫기
  };

  // 게시글 수정
  const handleEditPost = () => {
    if (!user || user.id !== board.writer) {
      alert('본인 글만 수정 가능합니다.');
      return;
    }
    navigate(`/PostFormWithComments/edit/${id}`);
    setShowDropdown(false); // 드롭다운 닫기
  };

  const startEditing = (commentId, text) => {
    setEditingCommentId(commentId);
    setEditingCommentText(text);
  };

  const saveEditComment = async () => {
    if (!editingCommentText.trim()) return;

    try {
      await api.put(`/comment/${id}/${editingCommentId}`, {
        text: editingCommentText
      });
      setEditingCommentId(null);
      setEditingCommentText('');
      const newCommentsLength = await fetchBoardDetail();
      setPrevCommentsLength(newCommentsLength);
    } catch (err) {
      console.error(err);
      alert('댓글 수정 실패');
    }
  };

  const cancelEdit = () => {
    setEditingCommentId(null);
    setEditingCommentText('');
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleCommentSubmit();
    }
  };

  // 더보기 메뉴 토글
  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

  if (loading) {
    return (
        <div className="board-detail-container">
          <div className="loading">게시글을 불러오는 중...</div>
        </div>
    );
  }

  if (!board) {
    return (
        <div className="board-detail-container">
          <div className="empty-state">게시글이 존재하지 않습니다.</div>
        </div>
    );
  }

  return (
      <div className="board-detail-container">
        <button className="back-button" onClick={() => navigate(-1)}>
          ⬅
        </button>

        {/* 게시글 상세 */}
        <div className="board-detail-content">
          <div className="board-detail-header">
            <h1 className="board-detail-title">{board.title}</h1>
            {user && user.id === board.writer && (
                <div className="board-actions" ref={dropdownRef}>
                  <button onClick={toggleDropdown} className="menu-button">
                    ⋯
                  </button>
                  <div className={`dropdown-menu ${!showDropdown ? 'hidden' : ''}`}>
                    <button onClick={handleEditPost} className="edit-btn">수정</button>
                    <button onClick={handleDeletePost} className="delete-btn">삭제</button>
                  </div>
                </div>
            )}
            <div className="board-detail-meta">
              <span>작성자: {board.writer}</span>
              <span>작성일: {formatDate(board.createdAt)}</span>
              <span>카테고리: {categoryTextMap[board.category] || board.category}</span>
            </div>
          </div>
          <div className="board-detail-body">
            {board.text}
          </div>
        </div>

        {/* 댓글 섹션 */}
        <div className="comments-section">
          <h3 style={{ marginBottom: '1.5rem', color: '#2c3e50' }}>
            댓글 ({comments.length})
          </h3>

          {/* 댓글 작성 */}
          <div className="comment-form">
          <textarea
              id="newComment"
              name="newComment"
              value={newComment}
              onChange={e => setNewComment(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder={user ? "댓글을 입력하세요 (Enter: 등록, Shift+Enter: 줄바꿈)" : "로그인 후 댓글 작성이 가능합니다."}
              rows={3}
              disabled={!user}
          />
            <button
                onClick={handleCommentSubmit}
                disabled={!user || !newComment.trim()}
            >
              댓글 작성
            </button>
            {!user && (
                <p style={{
                  color: '#718096',
                  fontSize: '0.9rem',
                  marginTop: '0.5rem'
                }}>
                  로그인 후 댓글 작성이 가능합니다.
                </p>
            )}
          </div>

          {/* 댓글 목록 */}
          <div className="comments-list">
            {comments.length === 0 ? (
                <div className="empty-state" style={{ margin: 0 }}>
                  댓글이 없습니다.
                </div>
            ) : (
                comments.map(c => (
                    <div key={c.commentId} className="comment-item">
                      {editingCommentId === c.commentId ? (
                          <div className="comment-edit">
                    <textarea
                        value={editingCommentText}
                        onChange={e => setEditingCommentText(e.target.value)}
                        rows={3}
                    />
                            <div className="comment-edit-actions">
                              <button className="save-btn" onClick={saveEditComment}>
                                저장
                              </button>
                              <button className="cancel-btn" onClick={cancelEdit}>
                                취소
                              </button>
                            </div>
                          </div>
                      ) : (
                          <>
                            <div className="comment-text">{c.text}</div>
                            <div className="comment-meta">
                      <span>
                        작성자: {c.writer} | {formatDate(c.date)}
                      </span>
                              {user && user.id === c.writer && (
                                  <div className="comment-actions">
                                    <button
                                        className="edit-btn"
                                        onClick={() => startEditing(c.commentId, c.text)}
                                    >
                                      수정
                                    </button>
                                    <button
                                        className="delete-btn"
                                        onClick={() => handleCommentDelete(c.commentId, c.writer)}
                                    >
                                      삭제
                                    </button>
                                  </div>
                              )}
                            </div>
                          </>
                      )}
                    </div>
                ))
            )}
            <div ref={commentsEndRef} />
          </div>
        </div>
      </div>
  );
};

export default KboBoardDetail;