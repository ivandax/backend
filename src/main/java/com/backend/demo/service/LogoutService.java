package com.backend.demo.service;

import com.backend.demo.model.InvalidToken;
import com.backend.demo.repository.InvalidTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class LogoutService {
    @Autowired
    private InvalidTokenRepository invalidTokenRepository;

    public void logout(String token) {
        InvalidToken invalidToken = new InvalidToken(token);
        invalidTokenRepository.save(invalidToken);
    }
}
