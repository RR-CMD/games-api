package com.osamah.games.game;

import com.osamah.games.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
public class Game extends BaseEntity {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "slug", unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate releaseDate;

    private String imageUrl;

    private Integer metacriticScore;

    @ElementCollection
    @CollectionTable(name = "game_genres", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "genre")
    @BatchSize(size = 50)
    private List<String> genres;

    @ElementCollection
    @CollectionTable(name = "game_platforms", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "platform")
    @BatchSize(size = 50)
    private List<String> platforms;

    @Column(nullable = false)
    private Double averageScore = 0.0;

    @Column(nullable = false)
    private Long totalAdded = 0L;

    @Column(nullable = false)
    private Long plannedCount = 0L;

    @Column(nullable = false)
    private Long playingCount = 0L;

    @Column(nullable = false)
    private Long completedCount = 0L;

    @Column(nullable = false)
    private Long droppedCount = 0L;

    @Builder
    public Game(String slug, String title, String description, LocalDate releaseDate, String imageUrl,
            Integer metacriticScore, List<String> genres, List<String> platforms) {
        this.slug = slug;
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.imageUrl = imageUrl;
        this.metacriticScore = metacriticScore;
        this.genres = genres;
        this.platforms = platforms;
    }

    ;
}