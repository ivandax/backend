package com.backend.demo.controller;

import com.backend.demo.dtos.RecoverPasswordDTO;
import com.backend.demo.dtos.SetNewPasswordDTO;
import com.backend.demo.dtos.SignUpDTO;
import com.backend.demo.dtos.VerificationTokenRequestDTO;
import com.backend.demo.service.LogoutService;
import com.backend.demo.service.UserService;
import com.backend.demo.service.mailing.SendgridEmailService;
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

    @Autowired
    private SendgridEmailService sendgridEmailService;

    @RequestMapping(value = "/sign-up", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createOrganizationAndUser(HttpServletRequest request,
                                          @RequestBody @Valid SignUpDTO dto) throws IOException {
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

    @RequestMapping(value = "/test-email", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void testEmail(@RequestBody Map<String, String> body) throws IOException {
        String email = body.get("email");
        System.out.println(email);
        sendgridEmailService.sendTestMessage(email);
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
            throw new BadRequestException("Token is missing");
        }
    }

    @RequestMapping(value = "/recover-password", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void recoverPassword(HttpServletRequest request,
                                @RequestBody @Valid RecoverPasswordDTO dto) throws IOException {
        String username = dto.getEmail();
        userService.recoverPassword(username, request);
    }

    @RequestMapping(value = "/set-new-password", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void setNewPassword(HttpServletResponse response,
                               @RequestBody @Valid SetNewPasswordDTO dto) throws IOException {
        String passwordRecoveryToken = dto.getPasswordRecoveryToken();
        String newPassword = dto.getNewPassword();
        userService.setNewPassword(response, passwordRecoveryToken, newPassword);
    }
}
