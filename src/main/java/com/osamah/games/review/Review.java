package com.osamah.games.review;

import com.osamah.games.common.BaseEntity;
import com.osamah.games.game.Game;
import com.osamah.games.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "game_id"})})
@Getter
@Setter
@NoArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer rating;

    @Builder
    public Review(User user, Game game, String content, Integer rating) {
        this.user = user;
        this.game = game;
        this.content = content;
        this.rating = rating;
    }


}