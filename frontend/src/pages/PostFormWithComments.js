// src/pages/PostFormWithComments.js
import React, { useState, useEffect, useContext } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/api";
import { AuthContext } from "../context/AuthContext";
import "../styles/PostForm2.css";

const categoryMap = {
  general: 1,
  kbo: 2,
  NPB: 3,
  mlb: 4,
  amateur: 5,
};

const PostFormWithComments = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);
  const draftKey = isEdit ? `postDraft_${id}` : "postDraft_new";

  const { user } = useContext(AuthContext);
  const [form, setForm] = useState({ title: "", text: "", category: "general" });
  const [writer, setWriter] = useState("");
  const [loading, setLoading] = useState(false);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");

  // 로그인 체크 및 draft / 기존 글 불러오기
  useEffect(() => {
    if (!user) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }
    setWriter(user?.id || '');

    // Removed draft loading to ensure blank form on new post
    // const savedDraft = localStorage.getItem(draftKey);
    // if (savedDraft) setForm(prev => ({ ...prev, ...JSON.parse(savedDraft) }));

    if (isEdit) fetchPost(id);
  }, [id, isEdit, navigate, user]);

  const fetchPost = async (postId) => {
    try {
      setLoading(true);
      const res = await api.get(`/board/${postId}`);
      const board = res.data.board;
      setForm({
        title: board.title || "",
        text: board.text || "",
        category: Object.entries(categoryMap).find(([, val]) => val === board.category)?.[0] || "general",
      });
      setComments(res.data.comments || []);
    } catch (err) {
      console.error(err);
      alert("글 불러오기 실패");
    } finally {
      setLoading(false);
    }
  };

  // draft 자동 저장
  useEffect(() => {
    const handler = setTimeout(() => {
      localStorage.setItem(draftKey, JSON.stringify(form));
    }, 500);
    return () => clearTimeout(handler);
  }, [form, draftKey]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const savePost = async () => {
    if (!user || !user.id) { 
      alert("로그인이 필요하거나 사용자 정보가 유효하지 않습니다.");
      return;
    }
    const { title, text, category } = form;
    if (!title.trim() || !text.trim()) { alert("제목과 내용을 입력해주세요."); return; }

    const requestData = { writer: user.id, title: title.trim(), text: text.trim(), category: categoryMap[category] };
    try {
      setLoading(true);
      if (isEdit) {
        await api.put(`/board/${id}`, requestData);
        alert("글이 수정되었습니다.");
      } else {
        await api.post("/board", requestData);
        alert("글이 등록되었습니다.");
        setForm({ title: "", text: "", category: "general" });
      }
      localStorage.removeItem(draftKey);
      navigate("/kboBoard");
    } catch (err) {
      console.error(err);
      alert("글 저장 실패");
    } finally { setLoading(false); }
  };

  // 댓글 작성
  const addComment = async () => {
    if (!newComment.trim()) return;
    if (!user) { alert("로그인 후 작성 가능합니다."); return; }
    try {
      const res = await api.post(`/board/${id}/comments`, { writer: parseInt(user.id, 10), text: newComment.trim() });
      setComments(prev => [...prev, res.data]);
      setNewComment("");
    } catch (err) {
      console.error(err);
      alert("댓글 작성 실패");
    }
  };

  // 댓글 삭제
  const deleteComment = async (commentId, commentWriter) => {
    if (!user || parseInt(user.id, 10) !== commentWriter) { alert("자신의 댓글만 삭제할 수 있습니다."); return; }
    if (!window.confirm("댓글을 삭제하시겠습니까?")) return;
    try {
      await api.delete(`/board/${id}/comments/${commentId}`);
      setComments(prev => prev.filter(c => c.id !== commentId));
    } catch (err) {
      console.error(err);
      alert("댓글 삭제 실패");
    }
  };

  return (
    <div className="container">
      <div className="input-group">
        <h2 style={{ textAlign: "center" }}>{isEdit ? "글 수정" : "글쓰기"}</h2>
        {loading && <p>로딩 중...</p>}
        <input type="text" name="title" value={form.title} onChange={handleChange} placeholder="제목" />
        <span>작성자</span>
        <input type="text" value={writer} readOnly />
        <label>
          종류{" "}
          <select name="category" value={form.category} onChange={handleChange}>
            <option value="general">자유</option>
            <option value="kbo">KBO</option>
            <option value="NPB">NPB</option>
            <option value="mlb">MLB</option>
            <option value="amateur">사회인야구</option>
          </select>
        </label>
        <textarea name="text" value={form.text} onChange={handleChange} rows={10} placeholder="내용" style={{ resize: "vertical" }} />
        <div className="button-group">
          <button onClick={savePost} disabled={loading}>{isEdit ? "수정" : "저장"}</button>
          <button onClick={() => { if (window.confirm("취소하면 임시 저장이 삭제됩니다.")) { localStorage.removeItem(draftKey); navigate("/kboBoard"); }}} disabled={loading}>취소</button>
        </div>

        {/* 댓글 */}
        {isEdit && (
          <div className="comments-section">
            <h3>댓글</h3>
            <div className="new-comment">
              <textarea value={newComment} onChange={(e) => setNewComment(e.target.value)} rows={3} placeholder="댓글을 입력하세요" />
              <button onClick={addComment}>작성</button>
            </div>
            <div className="comment-list">
              {comments.map((c) => (
                <div key={c.id} className="comment-item">
                  <p>{c.text}</p>
                  <small>{c.writer} | {c.createdAt}</small>
                  {user && parseInt(user.id, 10) === c.writer && (
                    <button onClick={() => deleteComment(c.id, c.writer)}>삭제</button>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PostFormWithComments;