import React, { useState, useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/api";
import { AuthContext } from "../context/AuthContext";
import '../styles/kboboard.css';

const categoryTextMap = { 1:"자유",2:"KBO",3:"NPB",4:"MLB",5:"사회인야구" };
const categoryValueMap = { all:null, general:1, kbo:2, NPB:3, mlb:4, amateur:5 };

const KboBoardList = () => {
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

  const [posts, setPosts] = useState([]);
  const [category, setCategory] = useState("all");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);

  const size = 10;

  // 게시글 불러오기
  const fetchPosts = async () => {
    setLoading(true);
    try {
      const res = await api.get("/board", {
        params: { category: categoryValueMap[category], keyword: keyword||null, page, size }
      });
      setPosts(res.data.list || []);
      setTotalPages(Math.ceil((res.data.totalCount || 0) / size));
    } catch(err){
      console.error(err);
      alert("게시글 로딩 실패");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPosts(); }, [category, keyword, page]);

  // 게시글 삭제
  const handleDelete = async (no, writerId) => {
    if (!user || user.id !== writerId?.toString()) {
      alert("본인 글만 삭제 가능합니다.");
      return;
    }
    if (!window.confirm("정말 삭제하시겠습니까?")) return;

    try {
      await api.delete(`/board/${no}`, {
        params: { writer: user.id }
      });
      alert("삭제 완료");
      fetchPosts();
    } catch(err){
      console.error(err);
      alert("삭제 실패");
    }
  };

  const handleSearch = () => {
    setPage(1);
    fetchPosts();
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
      <div className="noticeboard-container">
        <h2>KBO 게시판</h2>

        {/* 검색 및 필터 영역 */}
        <div className="board-controls">
          <select
              value={category}
              onChange={e => setCategory(e.target.value)}
          >
            <option value="all">전체</option>
            <option value="general">자유</option>
            <option value="kbo">KBO</option>
            <option value="NPB">NPB</option>
            <option value="mlb">MLB</option>
            <option value="amateur">사회인야구</option>
          </select>

          <input
              type="text"
              placeholder="검색어를 입력하세요"
              value={keyword}
              onChange={e => setKeyword(e.target.value)}
              onKeyPress={handleKeyPress}
          />

          <button onClick={handleSearch}>
            검색
          </button>

          <button
              className="write-button"
              onClick={() => navigate("/PostFormWithComments/new")}
          >
            글쓰기
          </button>
        </div>

        {/* 게시판 테이블 */}
        <div className="board-table-container">
          {loading ? (
              <div className="loading">게시글을 불러오는 중...</div>
          ) : posts.length === 0 ? (
              <div className="empty-state">게시글이 없습니다.</div>
          ) : (
              <table className="noticeboard-table">
                <thead>
                <tr>
                  <th>번호</th>
                  <th>제목</th>
                  <th>작성자</th>
                  <th>카테고리</th>
                  <th>작성일</th>
                  <th>삭제</th>
                </tr>
                </thead>
                <tbody>
                {posts.map((board) => (
                    <tr
                        key={board.no}
                        onClick={() => navigate(`/kboBoard/${board.no}`)}
                        style={{ cursor: "pointer" }}
                    >
                      <td>{board.no}</td>
                      <td>{board.title}</td>
                      <td>{board.writer}</td>
                      <td>{categoryTextMap[board.category] || board.category}</td>
                      <td>{new Date(board.createdAt).toLocaleDateString()}</td>
                      <td>
                        {user && user.id === board.writer.toString() && (
                            <button
                                className="delete-btn"
                                onClick={e => {
                                  e.stopPropagation();
                                  handleDelete(board.no, board.writer);
                                }}
                            >
                              삭제
                            </button>
                        )}
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
          )}
        </div>

        {/* 페이지네이션 */}
        {!loading && posts.length > 0 && (
            <div className="pagination">
              <button
                  disabled={page <= 1}
                  onClick={() => setPage(page - 1)}
              >
                이전
              </button>
              <span>{page} / {totalPages}</span>
              <button
                  disabled={page >= totalPages}
                  onClick={() => setPage(page + 1)}
              >
                다음
              </button>
            </div>
        )}
      </div>
  );
};

export default KboBoardList;