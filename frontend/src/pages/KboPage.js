import React, { useState, useEffect } from 'react';
import api from '../api/api';
import '../styles/kboPage.css';
import { convertInnings } from '../utils/inningsConverter';

const KboPage = () => {
  const [selectedTab, setSelectedTab] = useState('team');
  const [selectedHitter, setSelectedHitter] = useState('');

  const [hitterSortBy, setHitterSortBy] = useState('battingAverage');
  const [pitcherSortBy, setPitcherSortBy] = useState('era');
  const [pitcherSortDirection, setPitcherSortDirection] = useState('desc'); // 'desc' for descending, 'asc' for ascending

  const [teamStats, setTeamStats] = useState([]);
  const [hitterStats, setHitterStats] = useState([]);
  const [pitcherStats, setPitcherStats] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 타자 기록 정렬 기준을 업데이트하는 함수
  const handleHitterSort = (key) => {
    setHitterSortBy(key);
  };

  // 투수 기록 정렬 기준을 업데이트하는 함수
  const handlePitcherSort = (key) => {
    if (pitcherSortBy === key) {
      // If the same key is clicked, toggle sort direction
      setPitcherSortDirection(pitcherSortDirection === 'desc' ? 'asc' : 'desc');
    } else {
      // If a new key is clicked, set it as sortBy and default to descending
      setPitcherSortBy(key);
      setPitcherSortDirection('desc');
    }
  };

  useEffect(() => {
    let isMounted = true;
    const fetchData = async () => {
      setLoading(true);
      try {
        const teamResponse = await api.get('/kbo/team-stats');
        if (isMounted) {
          setTeamStats(teamResponse.data);
        }

        if (selectedTab === 'hitter' && selectedHitter === '타자') {
          const response = await api.get('/kbo/hitter-stats', {
            params: { sortBy: hitterSortBy }
          });
          if (isMounted) setHitterStats(response.data);
        } else if (selectedTab === 'hitter' && selectedHitter === '투수') {
          const response = await api.get('/kbo/pitcher-stats', {
            params: { sortBy: pitcherSortBy, sortDirection: pitcherSortDirection } // Pass sortDirection
          });
          if (isMounted) setPitcherStats(response.data);
        }
        if (isMounted) setError(null);
      } catch (err) {
        if (isMounted) setError('데이터를 불러오는 데 실패했습니다.');
        console.error(err);
      } finally {
        if (isMounted) setLoading(false);
      }
    };
    fetchData();

    return () => {
      isMounted = false;
    };
  }, [selectedTab, selectedHitter, hitterSortBy, pitcherSortBy, pitcherSortDirection]);

  const handleTabClick = (tab) => {
    setSelectedTab(tab);
    if (tab !== 'hitter') {
      setSelectedHitter('');
    } else {
      setSelectedHitter('타자');
    }
  };

  const renderTeamInfo = () => {
    if (loading) return <p>팀 순위 정보를 불러오는 중...</p>;
    if (error) return <p className="error">{error}</p>;

    return (

      <table className="record-table">
        <thead>
          <tr>
            <th>순위</th>
            <th>팀명</th>
            <th>경기 수</th>
            <th>승</th>
            <th>패</th>
            <th>무</th>
            <th>승률</th>
            <th>게임차</th>
          </tr>
        </thead>

        <tbody>
          {Array.isArray(teamStats) && teamStats.map((team, index) => (
            <tr key={index}>
              <td>{index + 1}</td>
              <td>{team.teamName}</td>
              <td>{team.gameNum}</td>
              <td>{team.win}</td>
              <td>{team.lose}</td>
              <td>{team.draw}</td>
              <td>{team.winPercentage}</td>
              <td>{team.gamesBehind}</td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  };

  const renderHitterInfo = (hitter) => {
    if (loading) return <p>선수 기록을 불러오는 중...</p>;
    if (error) return <p className="error">{error}</p>;

    const getTeamGameNum = (teamName) => {
      const team = teamStats.find(t => t.teamName === teamName);
      return team ? team.gameNum : 0;
    };

    switch (hitter) {
      case '타자':
        const hittersToRender = hitterStats;

        return (
          <table className="record-table">
            <thead>
              <tr>
                <th>순위</th>
                <th>선수명</th>
                <th>팀명</th>
                <th><button onClick={() => handleHitterSort('battingAverage')} className="sort-btn">타율</button></th>
                <th><button onClick={() => handleHitterSort('gameNum')} className="sort-btn">경기 수</button></th>
                <th><button onClick={() => handleHitterSort('plateAppearance')} className="sort-btn">타석</button></th>
                <th><button onClick={() => handleHitterSort('run')} className="sort-btn">득점</button></th>
                <th><button onClick={() => handleHitterSort('hit')} className="sort-btn">안타</button></th>
                <th><button onClick={() => handleHitterSort('twoBase')} className="sort-btn">2루타</button></th>
                <th><button onClick={() => handleHitterSort('threeBase')} className="sort-btn">3루타</button></th>
                <th><button onClick={() => handleHitterSort('homeRun')} className="sort-btn">홈런</button></th>
                <th><button onClick={() => handleHitterSort('runsBattedIn')} className="sort-btn">타점</button></th>
                <th><button onClick={() => handleHitterSort('fourBall')} className="sort-btn">볼넷</button></th>
                <th><button onClick={() => handleHitterSort('strikeOut')} className="sort-btn">삼진</button></th>
                <th><button onClick={() => handleHitterSort('onBasePercentage')} className="sort-btn">출루율</button></th>
                <th><button onClick={() => handleHitterSort('onbasePlusSlug')} className="sort-btn">OPS</button></th>
              </tr>
            </thead>
            <tbody>
              {Array.isArray(hittersToRender) && hittersToRender.map((hitter, index) => (
                <tr key={index}>
                  <td>{index + 1}</td>
                  <td>{hitter.name}</td>
                  <td>{hitter.team}</td>
                  <td>{hitter.battingAverage}</td>
                  <td>{hitter.gameNum}</td>
                  <td>{hitter.plateAppearance}</td>
                  <td>{hitter.run}</td>
                  <td>{hitter.hit}</td>
                  <td>{hitter.twoBase}</td>
                  <td>{hitter.threeBase}</td>
                  <td>{hitter.homeRun}</td>
                  <td>{hitter.runsBattedIn}</td>
                  <td>{hitter.fourBall}</td>
                  <td>{hitter.strikeOut}</td>
                  <td>{hitter.onBasePercentage}</td>
                  <td>{hitter.onbasePlusSlug}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );

      case '투수':
        const pitchersToRender = pitcherSortBy === 'era'
          ? pitcherStats.filter(pitcher => {
            const gameNum = getTeamGameNum(pitcher.team);
            return pitcher.inningsPitched >= gameNum * 1;
          })
          : pitcherStats;

        return (
          <table className="record-table">
            <thead>
              <tr>
                <th>순위</th>
                <th>선수명</th>
                <th>팀명</th>
                <th><button onClick={() => handlePitcherSort('era')} className="sort-btn">평균 자책점</button></th>
                <th><button onClick={() => handlePitcherSort('gameNum')} className="sort-btn">경기 수</button></th>
                <th><button onClick={() => handlePitcherSort('win')} className="sort-btn">승</button></th>
                <th><button onClick={() => handlePitcherSort('lose')} className="sort-btn">패</button></th>
                <th><button onClick={() => handlePitcherSort('save')} className="sort-btn">세이브</button></th>
                <th><button onClick={() => handlePitcherSort('hold')} className="sort-btn">홀드</button></th>
                <th><button onClick={() => handlePitcherSort('inningsPitched')} className="sort-btn">이닝</button></th>
                <th><button onClick={() => handlePitcherSort('hits')} className="sort-btn">피안타</button></th>
                <th><button onClick={() => handlePitcherSort('homeRun')} className="sort-btn">피홈런</button></th>
                <th><button onClick={() => handlePitcherSort('baseOnBalls')} className="sort-btn">볼넷</button></th>
                <th><button onClick={() => handlePitcherSort('strikeOut')} className="sort-btn">탈삼진</button></th>
                <th><button onClick={() => handlePitcherSort('runs')} className="sort-btn">실점</button></th>
                <th><button onClick={() => handlePitcherSort('earnedRun')} className="sort-btn">자책</button></th>
                <th><button onClick={() => handlePitcherSort('whip')} className="sort-btn">WHIP</button></th>
              </tr>
            </thead>
            <tbody>
              {Array.isArray(pitchersToRender) && pitchersToRender.map((pitcher, index) => (
                <tr key={index}>
                  <td>{index + 1}</td>
                  <td>{pitcher.name}</td>
                  <td>{pitcher.team}</td>
                  <td>{pitcher.earnedRunAverage}</td>
                  <td>{pitcher.gameNum}</td>
                  <td>{pitcher.win}</td>
                  <td>{pitcher.lose}</td>
                  <td>{pitcher.save}</td>
                  <td>{pitcher.hold}</td>
                  <td>{convertInnings(pitcher.inningsPitched)}</td>
                  <td>{pitcher.hits}</td>
                  <td>{pitcher.homeRun}</td>
                  <td>{pitcher.baseOnBalls}</td>
                  <td>{pitcher.strikeOut}</td>
                  <td>{pitcher.runs}</td>
                  <td>{pitcher.earnedRun}</td>
                  <td>{pitcher.whip}</td>
                </tr>
              ))}
            </tbody>
          </table>
        );

      default:
        return null;
    }
  };

  return (
    <div className="about-container">
      <h1>KBO</h1>

      <div className="player-record-container">

        <div className="record-buttons">
          <button className="team-record" onClick={() => handleTabClick('team')}>
            팀 순위
          </button>

          <button className="team-record" onClick={() => handleTabClick('hitter')}>
            선수 기록
          </button>
        </div>

        {selectedTab === 'team' && (
          <div className="team-info-container">{renderTeamInfo()}</div>
        )}

        {selectedTab === 'hitter' && (
          <div className="hitter-section">
            <div className="hitter-buttons">
              <button
                className={`hitter-btn ${selectedHitter === '타자' ? 'active' : ''}`}
                onClick={() => setSelectedHitter('타자')}
              >
                타자
              </button>

              <button
                className={`hitter-btn ${selectedHitter === '투수' ? 'active' : ''}`}
                onClick={() => setSelectedHitter('투수')}
              >
                투수
              </button>
            </div>

            <div className="hitter-info-container">{renderHitterInfo(selectedHitter)}</div>
          </div>
        )}
      </div>
    </div>
  );
};

export default KboPage;
