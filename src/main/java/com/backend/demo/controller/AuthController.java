package com.backend.demo.controller;

import com.backend.demo.dtos.SignUpDTO;
import com.backend.demo.dtos.VerificationTokenRequestDTO;
import com.backend.demo.service.LogoutService;
import com.backend.demo.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogoutService logoutService;

    @RequestMapping(value = "/sign-up", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createOrganizationAndUser(HttpServletRequest request,
                                          @RequestBody @Valid SignUpDTO dto) throws MessagingException {
        String username = dto.getEmail();
        String password = dto.getPassword();
        userService.createUserAndOrganization(username, password, request);
    }

    @RequestMapping(value = "/verify-token", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void verifyToken(HttpServletResponse response,
                            @RequestBody @Valid VerificationTokenRequestDTO dto) throws IOException {
        String verificationToken = dto.getVerificationToken();
        userService.verifyToken(response, verificationToken);
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.CREATED)
    public String authTest() {
        return new Date().toString();
    }

    @RequestMapping(value = "/test-for-admin-role", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> authTestForAdminRole() {
        Map<String, String> response = new HashMap<>();
        response.put("date", new Date().toString());
        response.put("message", "You have admin access.");
        return response;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request) throws BadRequestException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring("Bearer ".length());
            logoutService.logout(token);
        } else {
            throw new BadRequestException("Token is missing");
        }
    }

    @RequestMapping(value = "/renew-token", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> refreshTokens(HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String refreshToken = authorizationHeader.substring("Bearer ".length());
            return userService.getNewTokens(request, response, refreshToken);
        } else {
            throw new RuntimeException("Token is missing");
        }
    }
}
