package com.osamah.games.usergame;

import com.osamah.games.usergame.enums.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    @EntityGraph(attributePaths = {"game"})
    Optional<UserGame> findByUserIdAndGameId(Long userId, Long gameId);

    List<UserGame> findByUserId(Long id);

    @Query("""
            SELECT ug.gameStatus, COUNT(ug)
            FROM UserGame ug
            WHERE ug.user.id = :userId
            GROUP BY ug.gameStatus
            """)
    List<Object[]> statsByUserId(Long userId);

    @Query(value = """
            SELECT ug FROM UserGame ug
            JOIN FETCH ug.game g
            WHERE ug.user.id = :userId
            AND (:status IS NULL OR ug.gameStatus = :status)
            AND (:genre IS NULL OR :genre MEMBER OF g.genres)
            AND (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', CAST(:title AS text), '%')))
            AND (:startYear IS NULL OR YEAR(g.releaseDate) >= :startYear)
            AND (:endYear IS NULL OR YEAR(g.releaseDate) <= :endYear)
            """, countQuery = """
            SELECT COUNT(ug) FROM UserGame ug
            JOIN ug.game g
            WHERE ug.user.id = :userId
            AND (:status IS NULL OR ug.gameStatus = :status)
            AND (:genre IS NULL OR :genre MEMBER OF g.genres)
            AND (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', CAST(:title AS text), '%')))
            AND (:startYear IS NULL OR YEAR(g.releaseDate) >= :startYear)
            AND (:endYear IS NULL OR YEAR(g.releaseDate) <= :endYear)
            """)
    Page<UserGame> searchUserGames(@Param("userId") Long userId, @Param("status") GameStatus status,
            @Param("genre") String genre, @Param("title") String title, @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear, Pageable pageable);

}
