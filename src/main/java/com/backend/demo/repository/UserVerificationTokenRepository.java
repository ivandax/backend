package com.backend.demo.repository;

import com.backend.demo.model.User;
import com.backend.demo.model.UserVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVerificationTokenRepository extends JpaRepository<UserVerificationToken, Integer> {
    Optional<UserVerificationToken> findByBelongsTo(User user);
}
