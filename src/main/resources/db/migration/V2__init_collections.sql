CREATE TABLE game_genres
(
    game_id BIGINT NOT NULL,
    genre   VARCHAR(255),
    CONSTRAINT fk_game_genres_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
);

CREATE TABLE game_platforms
(
    game_id  BIGINT NOT NULL,
    platform VARCHAR(255),
    CONSTRAINT fk_game_platforms_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
);


CREATE INDEX idx_game_genres_game_id ON game_genres (game_id);
CREATE INDEX idx_game_platforms_game_id ON game_platforms (game_id);