package com.backend.demo.controller;

import com.backend.demo.dtos.SignUpDTO;
import com.backend.demo.dtos.VerificationTokenRequestDTO;
import com.backend.demo.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/sign-up", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createOrganizationAndUser(HttpServletRequest request,
                                          @RequestBody @Valid SignUpDTO dto) throws MessagingException {
        String username = dto.getEmail();
        String password = dto.getPassword();
        String organizationName = dto.getOrganizationName();
        userService.createUserAndOrganization(username, password, organizationName,
                request);
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
}
