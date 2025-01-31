package com.backend.demo.repository;

import com.backend.demo.model.PasswordRecoveryToken;
import com.backend.demo.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, Integer> {
    Optional<PasswordRecoveryToken> findByBelongsTo(User user);

    @Transactional
    @Modifying
    @Query(value="delete from PasswordRecoveryToken prt where prt.belongsTo = :user")
    void deleteWhereBelongsTo(User user);
}
