package com.backend.demo.service;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.backend.demo.config.ConfigProperties;
import com.backend.demo.config.CustomUserDetails;
import com.backend.demo.dtos.ResourceResponseDTO;
import com.backend.demo.dtos.User.UserResponseDTO;
import com.backend.demo.model.Permission;
import com.backend.demo.model.Role;
import com.backend.demo.model.User;
import com.backend.demo.model.UserVerificationToken;
import com.backend.demo.repository.OrganizationRepository;
import com.backend.demo.repository.RoleRepository;
import com.backend.demo.repository.UserRepository;
import com.backend.demo.repository.UserVerificationTokenRepository;
import com.backend.demo.service.mailing.EmailService;
import com.backend.demo.utils.JwtUtils;
import com.backend.demo.utils.PaginationUtils;
import com.backend.demo.utils.UserUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        User newUser = new User(username, password);
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

    public void verifyToken(HttpServletResponse response, String verificationToken) throws IOException {
        try {
            Algorithm algorithm =
                    Algorithm.HMAC256(configProperties.getVerificationSecret().getBytes());
            String username = JwtUtils.getUsernameFromJWT(algorithm, verificationToken);
            Optional<User> maybeUser = userRepository.findByUsername(username);
            if (maybeUser.isEmpty()) {
                throw new IllegalArgumentException("User not found");
            }
            User foundUser = maybeUser.get();
            Optional<UserVerificationToken> persistedVerificationToken =
                    userVerificationTokenRepository.findByBelongsTo(foundUser);
            if (persistedVerificationToken.isPresent()) {
                foundUser.setVerified(true);
                userRepository.save(foundUser);
                userVerificationTokenRepository.delete(persistedVerificationToken.get());
            } else {
                throw new IllegalArgumentException("Error with the provided verification token");
            }
        } catch (JWTVerificationException exception) {
            JwtUtils.catchVerificationTokenError(response, exception);
        }
    }

    public ResourceResponseDTO<UserResponseDTO> findAll(Integer page, Integer perPage,
                                                        String sortBy,
                                                        Sort.Direction sortDirection) {
        Pageable paginationConfig = PaginationUtils.getPaginationConfig(page, perPage, sortBy,
                sortDirection);
        Page<User> users = userRepository.findAll(paginationConfig);
        return new ResourceResponseDTO<>(
                users.stream().map(UserUtils::userToUserResponseDTO).collect(Collectors.toList()),
                users.getTotalPages(),
                PaginationUtils.getPage(page),
                PaginationUtils.getPerPage(perPage)
        );
    }

    public UserResponseDTO findLoggedInUser(CustomUserDetails userPrincipal) throws BadRequestException {
        Optional<User> maybeUser = userRepository.findByUsername(userPrincipal.getUsername());
        User user = maybeUser.orElseThrow(() -> new BadRequestException("User not found"));
        return UserUtils.userToUserResponseDTO(user);
    }

    public Map<String, String> getNewTokens(
            HttpServletRequest request,
            HttpServletResponse response,
            String refreshToken
    ) throws IOException {
        try {
            Algorithm algorithm =
                    Algorithm.HMAC256(configProperties.getAuthenticationSecret().getBytes());
            String requesterUsername = JwtUtils.getUsernameFromJWT(algorithm, refreshToken);
            User user =
                    userRepository.findByUsername(requesterUsername).orElseThrow(() -> new IllegalArgumentException("User could not be found"));
            Collection<String> permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions()
                            .stream().map(Permission::getPermissionName))
                    .collect(Collectors.toSet());
            CustomUserDetails userDetails = new CustomUserDetails(user, permissions);
            return JwtUtils.generateNewAccessToken(request, userDetails, algorithm, refreshToken);

        } catch (JWTVerificationException exception) {
            JwtUtils.catchJWTError(response, exception);
        }
        return null;
    }
}
