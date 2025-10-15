// Scoreboard.js
import React, { useMemo } from "react";

const Scoreboard = ({ gameState, homeTeam, awayTeam, lineups, inningCount }) => {
  const totalInnings = inningCount || 9;

  // ===== eventLog 기반 이닝별 점수 계산 (PA_END + GAME_END) =====
  const { homeScore, awayScore } = useMemo(() => {
    const homeScore = Array(totalInnings).fill(0);
    const awayScore = Array(totalInnings).fill(0);

    let lastHomeTotal = 0;
    let lastAwayTotal = 0;

    gameState.eventLog?.forEach(ev => {
      if (ev.type !== "PA_END" && ev.type !== "GAME_END") return;
      const inningIdx = ev.inning - 1;

      if (ev.top) {
        const increment = (ev.awayScore ?? lastAwayTotal) - lastAwayTotal;
        awayScore[inningIdx] += increment;
        lastAwayTotal = ev.awayScore ?? lastAwayTotal;
      } else {
        const increment = (ev.homeScore ?? lastHomeTotal) - lastHomeTotal;
        homeScore[inningIdx] += increment;
        lastHomeTotal = ev.homeScore ?? lastHomeTotal;
      }
    });

    return { homeScore, awayScore };
  }, [gameState.eventLog, totalInnings]);

  const isTop = (() => {
    const val = gameState?.isTop ?? gameState?.top ?? gameState?.offenseSide;
    if (typeof val === "boolean") return val;
    if (typeof val === "number") return val === 0;
    return String(val).toUpperCase() === "TOP";
  })();

  const homeTeamName = lineups?.home?.teamName || homeTeam || gameState?.homeTeam || "홈 팀";
  const awayTeamName = lineups?.away?.teamName || awayTeam || gameState?.awayTeam || "원정 팀";

  const homeBattingOrder = lineups?.home?.battingOrder?.map(p => p.name || p.Player_Name || p) || [];
  const awayBattingOrder = lineups?.away?.battingOrder?.map(p => p.name || p.Player_Name || p) || [];

  const homeBatterIndex = gameState?.homeBatterIndex ?? 0;
  const awayBatterIndex = gameState?.awayBatterIndex ?? 0;
  const currentPitcher = gameState?.currentPitcher;
  const currentBatter = gameState?.currentBatter;

  const getPlayer = (current, teamName) =>
    current?.team === teamName ? current.name : "-";

  const homePitcher = getPlayer(currentPitcher, homeTeamName) || lineups?.home?.pitcher || "-";
  const awayPitcher = getPlayer(currentPitcher, awayTeamName) || lineups?.away?.pitcher || "-";
  const homeBatter = getPlayer(currentBatter, homeTeamName) || homeBattingOrder[homeBatterIndex] || "-";
  const awayBatter = getPlayer(currentBatter, awayTeamName) || awayBattingOrder[awayBatterIndex] || "-";

  // ===== 테이블 표시 =====
  return (
    <div style={{ overflowX: "auto" }}>
      <table border="1" cellPadding="10" style={{ width: "100%", textAlign: "center", borderCollapse: "collapse" }}>
        <thead>
          <tr>
            <th>Team</th>
            {Array.from({ length: totalInnings }).map((_, i) => <th key={i}>{i + 1}</th>)}
            <th>R</th><th>H</th><th>B</th>
          </tr>
        </thead>
        <tbody>
          {/* 원정팀 */}
          <tr>
            <td>{awayTeamName}</td>
            {awayScore.map((s, i) => <td key={i}>{s}</td>)}
            <td style={{ fontWeight: "bold" }}>{awayScore.reduce((a, b) => a + b, 0)}</td>
            <td style={{ fontWeight: "bold" }}>{gameState?.awayHit ?? 0}</td>
            <td style={{ fontWeight: "bold" }}>{gameState?.awayWalks ?? 0}</td>
          </tr>

          {/* 홈팀 */}
          <tr>
            <td>{homeTeamName}</td>
            {homeScore.map((s, i) => <td key={i}>{s}</td>)}
            <td style={{ fontWeight: "bold" }}>{homeScore.reduce((a, b) => a + b, 0)}</td>
            <td style={{ fontWeight: "bold" }}>{gameState?.homeHit ?? 0}</td>
            <td style={{ fontWeight: "bold" }}>{gameState?.homeWalks ?? 0}</td>
          </tr>

          {/* 원정팀 라인업/투수 */}
          <tr>
            <td colSpan={totalInnings + 5} style={{ backgroundColor: "#fff0e0" }}>
              {isTop
                ? `${awayTeamName} - 타자: ${awayBatter}`
                : `${awayTeamName} - 투수: ${awayPitcher}`}
            </td>
          </tr>

          {/* 홈팀 라인업/투수 */}
          <tr>
            <td colSpan={totalInnings + 5} style={{ backgroundColor: "#e0f0ff" }}>
              {isTop
                ? `${homeTeamName} - 투수: ${homePitcher}`
                : `${homeTeamName} - 타자: ${homeBatter}`}
            </td>
          </tr>

          {/* 현재 이닝 */}
          <tr>
            <td colSpan={totalInnings + 5} style={{ fontWeight: "bold" }}>
              {gameState.inning}회 {isTop ? "초" : "말"}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );
};

export default Scoreboard;
