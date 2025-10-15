// BaseballGameSimulator.js
import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import Scoreboard from '../pages/Scoreboard';
import img111 from '../styles/111.png';

const BaseballGameSimulator = () => {
  const [inningCount, setInningCount] = useState(9); // ✅ 게임 이닝 수 선택
  const [gameState, setGameState] = useState({
    inning: 1,
    isTop: true,
    balls: 0,
    strikes: 0,
    outs: 0,
    score: { my: Array(9).fill(0), opponent: Array(9).fill(0) },
    bases: [false, false, false],
  });
  const [message, setMessage] = useState("");
  const [pitchGauge, setPitchGauge] = useState(0);
  const [swingGauge, setSwingGauge] = useState(0);
  const [animating, setAnimating] = useState(false);
  const [gameStarted, setGameStarted] = useState(false);
  const [shots, setShots] = useState([]);
  const [currentType, setCurrentType] = useState(null);
  const gaugeInterval = useRef(null);

  const CM_IN_PX = 38;
  const STRIKE_ZONE_SIZE = 220;
  const LEFT_COL_WIDTH = 320;
  const RIGHT_COL_WIDTH = 260;
  const CENTER_MAX_WIDTH = 560;

  // 게임 시작
  const startGame = () => {
    setGameState(prev => ({
      ...prev,
      score: {
        my: Array(inningCount).fill(0),
        opponent: Array(inningCount).fill(0)
      }
    }));
    setGameStarted(true);
  };

  // 게이지 시작/클릭
  const startGauge = (type) => {
    if (!animating) {
      setCurrentType(type);
      setAnimating(true);
      let val = 0;
      gaugeInterval.current = setInterval(() => {
        val = Math.min(val + Math.floor(Math.random() * 6 + 1), 100);
        if (type === "pitch") setPitchGauge(val);
        else setSwingGauge(val);
        if (val >= 100) stopGauge();
      }, Math.floor(Math.random() * 25 + 15));
    } else if (currentType === type) {
      stopGauge();
    }
  };

  const stopGauge = async () => {
    if (!animating) return;
    clearInterval(gaugeInterval.current);
    setAnimating(false);
    const gaugeVal = currentType === "pitch" ? pitchGauge : swingGauge;
    const type = currentType;
    setCurrentType(null);

    setMessage(`${type === "pitch" ? "투구" : "타격"} 완료! 게이지: ${gaugeVal.toFixed(0)}`);

    let updatedState = { ...gameState };
    if (type === "pitch") {
      const isStrike = Math.random() < gaugeVal / 100;
      if (isStrike) updatedState.strikes = Math.min(updatedState.strikes + 1, 3);
      else updatedState.balls = Math.min(updatedState.balls + 1, 4);
    } else {
      const isHit = Math.random() < gaugeVal / 100;
      if (isHit) {
        const team = updatedState.isTop ? "opponent" : "my";
        updatedState.score[team][updatedState.inning - 1] += 1;
      }
    }
    setGameState(updatedState);

    try {
      await axios.post(`/api/game/update`, { type, gaugeValue: gaugeVal, gameState: updatedState });
    } catch (err) {
      console.error("게임 상태 전송 실패:", err);
    }
  };

  // 스트라이크존 클릭
  const handleStrikeZoneClick = async (e) => {
    const wrapper = document.getElementById("strike-zone");
    if (!wrapper) return;

    const rect = wrapper.getBoundingClientRect();
    const exX = e.clientX - rect.left;
    const exY = e.clientY - rect.top;

    const relX = exX - CM_IN_PX;
    const relY = exY - CM_IN_PX;

    const insideCore = relX >= 0 && relY >= 0 && relX <= STRIKE_ZONE_SIZE && relY <= STRIKE_ZONE_SIZE;
    const insideExtended = exX >= 0 && exY >= 0 && exX <= STRIKE_ZONE_SIZE + CM_IN_PX * 2 && exY <= STRIKE_ZONE_SIZE + CM_IN_PX * 2;

    const updatedShots = shots.map(s => ({ ...s, color: "gray" }));
    const color = insideCore ? "blue" : insideExtended ? "green" : "red";
    const newShot = { relX: exX, relY: exY, color, insideCore, insideExtended };
    setShots([...updatedShots, newShot]);

    try {
      await axios.post("/api/game/strikezone", newShot);
    } catch (err) {
      console.error("클릭 전송 실패:", err);
    }

    if (currentType === "pitch") {
      let updatedState = { ...gameState };
      if (!insideCore) updatedState.balls = Math.min(updatedState.balls + 1, 4);
      else updatedState.strikes = Math.min(updatedState.strikes + 1, 3);
      setGameState(updatedState);
    }
  };

  // 1루/2루/3루 클릭
  const toggleBase = async (index) => {
    const newBases = [...gameState.bases];
    newBases[index] = !newBases[index];
    setGameState(prev => ({ ...prev, bases: newBases }));

    try {
      await axios.post("/api/game/bases", { bases: newBases, inning: gameState.inning, isTop: gameState.isTop });
    } catch (err) {
      console.error("주자 상태 전송 실패:", err);
    }
  };

  // 백엔드 상태 주기적 반영
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const res = await axios.get("/api/game/state");
        const { score, bases, inning, isTop, strikes, balls, outs } = res.data;
        setGameState(prev => ({ ...prev, score, bases, inning, isTop, strikes, balls, outs }));
      } catch (err) {
        console.error("게임 상태 불러오기 실패:", err);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      {!gameStarted && (
        <>
          <label>
            게임 이닝 수:
            <select value={inningCount} onChange={(e) => setInningCount(Number(e.target.value))}>
              {Array.from({ length: 9 }, (_, i) => i + 1).map((num) => (
                <option key={num} value={num}>{num} 이닝</option>
              ))}
            </select>
          </label>
          <button onClick={startGame}>게임 시작</button>
        </>
      )}

      {gameStarted && (
        <>
          <p>📢 {message}</p>
          <div style={{ display: 'flex', gap: 24, alignItems: 'flex-start', marginTop: 20 }}>
            {/* 왼쪽 점수판 */}
            <div style={{ width: LEFT_COL_WIDTH }}>
              <table border="1" cellPadding="5" style={{ width: '100%' }}>
                <thead>
                  <tr>
                    <th>이닝</th>
                    {Array.from({ length: inningCount }).map((_, i) => <th key={i}>{i+1}</th>)}
                    <th>합계</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>홈 팀</td>
                    {gameState.score.my.map((s,i) => <td key={i}>{s}</td>)}
                    <td>{gameState.score.my.reduce((a,b)=>a+b,0)}</td>
                  </tr>
                  <tr>
                    <td>원정 팀</td>
                    {gameState.score.opponent.map((s,i) => <td key={i}>{s}</td>)}
                    <td>{gameState.score.opponent.reduce((a,b)=>a+b,0)}</td>
                  </tr>
                  <tr>
                    <td colSpan={inningCount + 2}>이닝: {gameState.inning} {gameState.isTop ? "초" : "말"}</td>
                  </tr>
                </tbody>
              </table>
              <div style={{ marginTop: 20 }}>
                <Scoreboard 
                  strike={gameState.strikes}
                  ball={gameState.balls}
                  out={gameState.outs}
                  innings={gameState.score}
                  bases={gameState.bases}
                />
              </div>
            </div>

            {/* 가운데 이미지 + 주자 */}
            <div style={{ flex: 1, minWidth: 420, maxWidth: CENTER_MAX_WIDTH, display: 'flex', justifyContent: 'center', alignItems: 'center', position: 'relative' }}>
              <img src={img111} style={{ width: '100%', height: 'auto', maxWidth: CENTER_MAX_WIDTH }} />
              {[0,1,2].map(i => {
                const positions = [
                  { left: '72%', top: '52%' },
                  { left: '45%', top: '35%' },
                  { left: '20%', top: '52%' }
                ];
                return (
                  <div key={i} 
                    style={{
                      position: 'absolute', width: 40, height: 40, border: '2px solid #333',
                      background: gameState.bases[i] ? 'yellow' : 'white', display:'flex', justifyContent:'center', alignItems:'center',
                      transform:'rotate(45deg)', left:positions[i].left, top:positions[i].top, cursor:'pointer'
                    }}
                    onClick={() => toggleBase(i)}
                  >
                    <span style={{ transform:'rotate(-45deg)', color: gameState.bases[i] ? '#000' : '#666' }}>{i+1}루</span>
                  </div>
                )
              })}
            </div>

            {/* 오른쪽 컨트롤 + 스트라이크존 */}
            <div style={{ width: RIGHT_COL_WIDTH }}>
              <div style={{ marginBottom: 10 }}>
                <button onClick={() => startGauge("pitch")}>
                  {animating && currentType === "pitch" ? "던지기 클릭!" : "던지기 시작"}
                </button>
                <button onClick={() => startGauge("swing")} style={{ marginLeft: 10 }}>
                  {animating && currentType === "swing" ? "치기 클릭!" : "치기 시작"}
                </button>
              </div>

              <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
                <div>
                  <div style={{ marginBottom:5 }}>치기</div>
                  <div style={{ width:'100%', height:20, background:'#eee', borderRadius:10 }}>
                    <div style={{ width:`${swingGauge}%`, height:'100%', background:'#1E90FF', borderRadius:10 }}></div>
                  </div>
                </div>
                <div>
                  <div style={{ marginBottom:5 }}>투구</div>
                  <div style={{ width:'100%', height:20, background:'#eee', borderRadius:10 }}>
                    <div style={{ width:`${pitchGauge}%`, height:'100%', background:'red', borderRadius:10 }}></div>
                  </div>
                </div>
              </div>

              {/* 스트라이크존 + 회색 영역 */}
              <div id="strike-zone" onClick={handleStrikeZoneClick} style={{
                marginTop:20, width:STRIKE_ZONE_SIZE + CM_IN_PX*2, height:STRIKE_ZONE_SIZE + CM_IN_PX*2,
                position:'relative', cursor:'crosshair', overflow:'visible', border:'1px solid #ccc'
              }}>
                <div style={{
                  position:'absolute', top:CM_IN_PX, left:CM_IN_PX,
                  width:STRIKE_ZONE_SIZE, height:STRIKE_ZONE_SIZE,
                  border:'2px solid black', display:'grid', gridTemplateColumns:'repeat(3,1fr)', gridTemplateRows:'repeat(3,1fr)'
                }}>
                  {[...Array(9)].map((_, i) => (
                    <div key={i} style={{
                      borderRight:i%3!==2?'1px dashed gray':'none',
                      borderBottom:i<6?'1px dashed gray':'none'
                    }}></div>
                  ))}
                </div>

                {shots.map((shot, idx) => (
                  <div key={idx} style={{
                    position:'absolute', width:6, height:6, borderRadius:'50%',
                    backgroundColor:shot.color, top:shot.relY-3, left:shot.relX-3
                  }}></div>
                ))}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default BaseballGameSimulator;
