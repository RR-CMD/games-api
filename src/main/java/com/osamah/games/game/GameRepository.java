package com.osamah.games.game;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findBySlug(String slug);

    @Modifying
    @Query("""
            UPDATE Game g
            SET g.averageScore = COALESCE(
                (SELECT AVG(ug.rating) FROM UserGame ug WHERE ug.game.id = g.id AND ug.rating IS NOT NULL),
                0.0
            )
            """)
    void updateAllAverageScores();

    @Query("""
            SELECT DISTINCT g FROM Game g
            LEFT JOIN g.genres gen
            LEFT JOIN g.platforms plat
            WHERE (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', CAST(:title AS text), '%')))
            AND (:genres IS NULL OR gen IN :genres)
            AND (:platforms IS NULL OR plat IN :platforms)
            AND (:startYear IS NULL OR YEAR(g.releaseDate) >= :startYear)
            AND (:endYear IS NULL OR YEAR(g.releaseDate) <= :endYear)
            AND (:minAverageScore IS NULL OR g.averageScore >= :minAverageScore)
            GROUP BY g.id
            HAVING (:genres IS NULL OR COUNT(DISTINCT gen) = :genreCount)
            """)
    Page<Game> searchGames(@Param("title") String title, @Param("genres") List<String> genres,
            @Param("platforms") List<String> platforms, @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear, @Param("minAverageScore") Double minAverageScore,
            @Param("genreCount") Long genreCount, Pageable pageable);
}