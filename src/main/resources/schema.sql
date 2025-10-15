DROP ALL OBJECTS;

-- Drop all tables first to ensure a clean slate
DROP TABLE IF EXISTS kbo_hitter_stats_2024;
DROP TABLE IF EXISTS kbo_pitcher_stats_2024;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS board;
DROP TABLE IF EXISTS kbo_team_stats_2024;
DROP TABLE IF EXISTS kbo_hitter_stats_2025;
DROP TABLE IF EXISTS kbo_pitcher_stats_2025;
DROP TABLE IF EXISTS kbo_team_stats_2025;
DROP TABLE IF EXISTS custom_lineup;
DROP TABLE IF EXISTS member;

-- Game-related tables for 2024 season
CREATE TABLE kbo_hitter_stats_2024 (
    No INT AUTO_INCREMENT PRIMARY KEY,
    Player_Name VARCHAR(50) NOT NULL,
    Player_Team VARCHAR(50) NOT NULL,
    Batting_average DECIMAL(4, 3) NOT NULL,
    Game_Num INT NOT NULL,
    Plate_Appearance INT NOT NULL,
    At_Bat INT NULL,
    Run INT NOT NULL,
    Hit INT NOT NULL,
    two_Base INT NOT NULL,
    three_Base INT NOT NULL,
    Home_Run INT NOT NULL,
    Total_Base INT NULL,
    Runs_Batted_In INT NOT NULL,
    Sacrifice_Bunts INT NULL,
    Sacrifice_Fly INT NULL,
    Four_Ball INT NOT NULL,
    IBB INT NULL,
    Hit_by_Pitch INT NULL,
    Strike_Out INT NOT NULL,
    Double_out INT NULL,
    Slugging DECIMAL(4, 3) NULL,
    On_Base_Percentage DECIMAL(4, 3) NOT NULL,
    Onbase_Plus_Slug DECIMAL(4, 3) NOT NULL,
    Multi_Hit INT NULL,
    Scoring_Position_AVG DECIMAL(4, 3) NULL,
    Pinch_Hit_AVG DECIMAL(4, 3) NULL,
    UNIQUE (Player_Name, Player_Team)
);

CREATE TABLE kbo_pitcher_stats_2024 (
    No INT AUTO_INCREMENT PRIMARY KEY,
    Player_Name VARCHAR(50) NOT NULL,
    Player_Team VARCHAR(50) NOT NULL,
    Earned_Run_Average DECIMAL(5, 2) NOT NULL,
    Game_Num INT NOT NULL,
    Win INT NOT NULL,
    Lose INT NOT NULL,
    Save INT NOT NULL,
    Hold INT NOT NULL,
    Winning_Percentage DECIMAL(4, 3) NULL,
    Innings_Pitched DECIMAL(6, 3) NOT NULL,
    Hits INT NOT NULL,
    Home_Run INT NOT NULL,
    Base_On_Balls INT NOT NULL,
    Hit_By_Pitch INT NULL,
    Strike_Out INT NOT NULL,
    Runs INT NOT NULL,
    Earned_Run INT NOT NULL,
    WHIP DECIMAL(4, 2) NOT NULL,
    Complete_Game INT NULL,
    Shutout INT NULL,
    Quality_Start INT NULL,
    Blown_Save INT NULL,
    Total_Batters_Faced INT NULL,
    Number_Of_Pitching INT NULL,
    Opponent_Batting_Average DECIMAL(4, 3) NULL,
    two_Base INT NULL,
    three_Base INT NULL,
    Sacrifice_Bunt INT NULL,
    Sacrifice_Fly INT NULL,
    IBB INT NULL,
    Wild_Pitch INT NULL,
    Balk INT NULL,
    UNIQUE (Player_Name, Player_Team)
);

CREATE TABLE kbo_team_stats_2024 (
    No INT AUTO_INCREMENT PRIMARY KEY,
    Team_Name VARCHAR(50) NOT NULL UNIQUE,
    Game_Num INT,
    Win INT,
    Lose INT,
    Draw INT,
    Win_Percentage DECIMAL(5, 3),
    Games_Behind DECIMAL(4, 1)
);

-- Homepage display tables for 2025 season
CREATE TABLE kbo_hitter_stats_2025 (
    No INT AUTO_INCREMENT PRIMARY KEY,
    Player_Name VARCHAR(50) NOT NULL,
    Player_Team VARCHAR(50) NOT NULL,
    Batting_average DECIMAL(4, 3) NOT NULL,
    Game_Num INT NOT NULL,
    Plate_Appearance INT NOT NULL,
    Run INT NOT NULL,
    Hit INT NOT NULL,
    two_Base INT NOT NULL,
    three_Base INT NOT NULL,
    Home_Run INT NOT NULL,
    Runs_Batted_In INT NOT NULL,
    Four_Ball INT NOT NULL,
    Strike_Out INT NOT NULL,
    On_Base_Percentage DECIMAL(4, 3) NOT NULL,
    Onbase_Plus_Slug DECIMAL(4, 3) NOT NULL
);

CREATE TABLE kbo_pitcher_stats_2025 (
    No INT AUTO_INCREMENT PRIMARY KEY,
    Player_Name VARCHAR(50) NOT NULL,
    Player_Team VARCHAR(50) NOT NULL,
    Earned_Run_Average DECIMAL(5, 2) NOT NULL,
    Game_Num INT NOT NULL,
    Win INT NOT NULL,
    Lose INT NOT NULL,
    Save INT NOT NULL,
    Hold INT NOT NULL,
    Innings_Pitched DECIMAL(6, 3) NOT NULL,
    Hits INT NOT NULL,
    Home_Run INT NOT NULL,
    Base_On_Balls INT NOT NULL,
    Strike_Out INT NOT NULL,
    Runs INT NOT NULL,
    Earned_Run INT NOT NULL,
    WHIP DECIMAL(4, 2) NOT NULL
);

CREATE TABLE kbo_team_stats_2025 (
    No INT AUTO_INCREMENT PRIMARY KEY,
    Team_Name VARCHAR(50) NOT NULL,
    Game_Num INT,
    Win INT,
    Lose INT,
    Draw INT,
    Win_Percentage DECIMAL(5, 3),
    Games_Behind DECIMAL(5, 2)
);

-- Other tables
CREATE TABLE board (
    no INT AUTO_INCREMENT PRIMARY KEY,

    category VARCHAR(255),
    title VARCHAR(255),
    text TEXT,
    writer VARCHAR(255),
    view INT DEFAULT 0,
    CREATEDAT DATETIME,
    UPDATEDAT DATETIME,
    keyword VARCHAR(255)
);

CREATE TABLE comment (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    board_no INT NOT NULL,
    writer VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (board_no) REFERENCES board(no) ON DELETE CASCADE
);

CREATE TABLE custom_lineup (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    team_name VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    player_name VARCHAR(255) NOT NULL,
    player_id VARCHAR(255) NOT NULL
);

CREATE TABLE member (
    id VARCHAR(255) PRIMARY KEY,
    pw VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL UNIQUE,
    game INT DEFAULT 0,
    win INT DEFAULT 0,
    lose INT DEFAULT 0,
    draw INT DEFAULT 0
);