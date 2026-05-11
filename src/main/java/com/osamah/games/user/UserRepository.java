package com.osamah.games.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    @Query("""
              SELECT u FROM User u WHERE 
              LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))
              ORDER BY u.username
            """)
    Page<User> searchUsers(@Param("username") String username, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);


}
