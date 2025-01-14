package com.backend.demo.repository;

import com.backend.demo.model.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, Integer> {
    InvalidToken findByToken(String token);
}
