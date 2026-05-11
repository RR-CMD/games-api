package com.osamah.games.usergame;

import com.osamah.games.common.BaseEntity;
import com.osamah.games.game.Game;
import com.osamah.games.user.User;
import com.osamah.games.usergame.enums.GameStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_games", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "game_id"})})
public class UserGame extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private GameStatus gameStatus;

    @Column
    @Setter
    private Integer rating;

    @Builder
    public UserGame(User user, Game game, GameStatus gameStatus, Integer rating) {
        this.user = user;
        this.game = game;
        this.gameStatus = gameStatus;
        this.rating = rating;
    }
}

