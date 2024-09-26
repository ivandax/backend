package com.backend.demo.service;

import com.auth0.jwt.algorithms.Algorithm;
import com.backend.demo.config.ConfigProperties;
import com.backend.demo.model.Organization;
import com.backend.demo.model.Role;
import com.backend.demo.model.User;
import com.backend.demo.model.UserVerificationToken;
import com.backend.demo.repository.OrganizationRepository;
import com.backend.demo.repository.RoleRepository;
import com.backend.demo.repository.UserRepository;
import com.backend.demo.repository.UserVerificationTokenRepository;
import com.backend.demo.service.mailing.EmailService;
import com.backend.demo.utils.JwtUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserVerificationTokenRepository userVerificationTokenRepository;

    @Autowired
    private ConfigProperties configProperties;

    public void createUserAndOrganization(String username,
                                          String password,
                                          String organizationName,
                                          HttpServletRequest request)
            throws MessagingException {
        Role adminRole = roleRepository.findByRoleName("ADMIN");
        if (adminRole == null) {
            throw new RuntimeException("Roles are not yet created");
        }
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            throw new IllegalArgumentException("This username already exists");
        }
        Organization newOrganization = new Organization(organizationName, 10);
        organizationRepository.save(newOrganization);

        User newUser = new User(username, password);
        newUser.setOrganization(newOrganization);
        newUser.setRoles(List.of(adminRole));
        userRepository.save(newUser);

        Algorithm algorithm =
                Algorithm.HMAC256(configProperties.getVerificationSecret().getBytes());
        String verificationToken = JwtUtils.generateUserVerificationToken(request, username,
                algorithm);
        UserVerificationToken userVerificationToken = new UserVerificationToken(newUser,
                verificationToken);
        userVerificationTokenRepository.save(userVerificationToken);

        emailService.sendUserVerificationTokenMessage(username,
                "Taskmaster: Please verify your account", verificationToken);
    }
}
