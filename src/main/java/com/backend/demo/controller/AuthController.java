package com.backend.demo.controller;

import com.backend.demo.dtos.SignUpDTO;
import com.backend.demo.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
}
