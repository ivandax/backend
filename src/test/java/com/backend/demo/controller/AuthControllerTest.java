package com.backend.demo.controller;

import com.backend.demo.dtos.RecoverPasswordDTO;
import com.backend.demo.dtos.SetNewPasswordDTO;
import com.backend.demo.model.PasswordRecoveryToken;
import com.backend.demo.model.Role;
import com.backend.demo.model.UserVerificationToken;
import com.backend.demo.repository.*;
import com.backend.demo.model.User;
import com.backend.demo.service.mailing.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserVerificationTokenRepository userVerificationTokenRepository;

    @Autowired
    private InvalidTokenRepository invalidTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    @BeforeEach
    void setup() {
        invalidTokenRepository.deleteAll();
        userVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        organizationRepository.deleteAll();

        Role dev = new Role("DEV");
        Role admin = new Role("ADMIN");
        roleRepository.saveAll(List.of(dev, admin));

        User devUser = new User();
        devUser.addRole(dev);
        devUser.setUsername("dev@mail.com");
        devUser.setPassword("testPassword");
        userRepository.save(devUser);

        User adminUser = new User();
        adminUser.addRole(admin);
        adminUser.setUsername("admin@mail.com");
        adminUser.setPassword("testPassword");
        adminUser.setVerified(true);
        userRepository.save(adminUser);
    }

    @Test
    @DisplayName("Failure: Sign up with wrong method - GET")
    public void signUpFailureByMethod() throws Exception {
        record SignUpRequest(String username, String password, String organizationName) {
        }

        SignUpRequest request = new SignUpRequest("dev@mail.com", "test1234", "Test Org");

        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(get("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

    }

    @Test
    @DisplayName("Failure: Sign up with empty body")
    public void signUpFailureBEmptyBody() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("Required request body " +
                "is missing"));
    }

    @Test
    @DisplayName("Failure: Badly formatted email")
    public void signUpFailureOnBadEmailAddress() throws Exception {

        record SignUpRequest(String email, String password, String organizationName) {
        }

        SignUpRequest request = new SignUpRequest("test", "test1234", "Test Org");

        String payload = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("Could not validate " +
                "provided data. Please review data sent"));
        assertTrue(mvcResult.getResponse().getContentAsString().contains("must be a well-formed " +
                "email address"));
    }

    @Test
    @DisplayName("Failure: User already exists")
    public void signUpFailureUserAlreadyExists() throws Exception {

        record SignUpRequest(String email, String password, String organizationName) {
        }

        SignUpRequest request = new SignUpRequest("dev@mail.com", "test1234", "Test Org");

        String payload = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("This username already " +
                "exists"));
    }

    @Test
    @DisplayName("Success: Sign up with username and password")
    public void signUpSuccess() throws Exception {

        record SignUpRequest(String email, String password, String organizationName) {
        }

        SignUpRequest request = new SignUpRequest("test@mail.com", "test1234", "Test Org");

        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals(3, userRepository.findAll().size());
    }

    @Test
    @DisplayName("Success: Sign up and verify token")
    public void signUpSuccessAndVerifyToken() throws Exception {

        record SignUpRequest(String email, String password, String organizationName) {
        }

        SignUpRequest request = new SignUpRequest("test@mail.com", "test1234", "Test Org");

        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        Optional<User> maybeUser = userRepository.findByUsername("test@mail.com");


        if (maybeUser.isEmpty()) {
            fail("User should be created");
        }
        User foundUser = maybeUser.get();
        assertFalse(foundUser.isVerified());

        Optional<UserVerificationToken> maybePersistedToken =
                userVerificationTokenRepository.findByBelongsTo(maybeUser.get());
        if (maybePersistedToken.isEmpty()) {
            fail("Token should be created");
        }
        UserVerificationToken token = maybePersistedToken.get();

        record VerificationTokenRequest(String verificationToken) {
        }

        VerificationTokenRequest verificationTokenRequest =
                new VerificationTokenRequest(token.getVerificationToken());

        String verificationTokenPayload = objectMapper.writeValueAsString(verificationTokenRequest);

        mockMvc.perform(post("/api/auth/verify-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verificationTokenPayload))
                .andExpect(status().isOk())
                .andReturn();

        Optional<User> reloadedMaybeUser = userRepository.findByUsername("test@mail.com");

        assertTrue(reloadedMaybeUser.isPresent() && reloadedMaybeUser.get().isVerified());

    }

    @Test
    @DisplayName("Failure: Renew token with wrong method - GET")
    public void renewTokenFailureWrongMethod() throws Exception {
        mockMvc.perform(get("/api/auth/renew-token")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "dev@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Failure: Renew token without header")
    public void renewTokenFailureNoHeader() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/renew-token"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("Token is missing"));
    }

    @Test
    @DisplayName("Success: Renew token")
    public void renewTokenSuccess() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String refreshToken = tokensResponse.get("refresh_token");

        MvcResult result = mockMvc.perform(post("/api/auth/renew-token")
                        .header("authorization",
                                "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("access_token"));
        assertTrue(result.getResponse().getContentAsString().contains("refresh_token"));
    }


    @Test
    @DisplayName("Failure: Recover Password with Wrong HTTP Method")
    public void recoverPasswordFailureByMethod() throws Exception {
        RecoverPasswordDTO dto = new RecoverPasswordDTO("dev@mail.com");
        String payload = objectMapper.writeValueAsString(dto);

        mockMvc.perform(get("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Failure: Recover Password with Empty Body")
    public void recoverPasswordFailureEmptyBody() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("Required request body " +
                "is missing"));
    }

    @Test
    @DisplayName("Failure: Recover Password with Invalid Email Format")
    public void recoverPasswordFailureInvalidEmail() throws Exception {
        RecoverPasswordDTO dto = new RecoverPasswordDTO("invalid-email");
        String payload = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("must be a well-formed " +
                "email address"));
    }

    @Test
    @DisplayName("Failure: Recover Password for Non-Existent User")
    public void recoverPasswordFailureNonExistentUser() throws Exception {
        RecoverPasswordDTO dto = new RecoverPasswordDTO("nonexistent@mail.com");
        String payload = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("This username was not " +
                "found"));
    }

    @Test
    @DisplayName("Success: Recover Password for Existing User")
    public void recoverPasswordSuccess() throws Exception {
        RecoverPasswordDTO dto = new RecoverPasswordDTO("admin@mail.com");
        String payload = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Failure: Set New Password with Wrong HTTP Method")
    public void setNewPasswordFailureByMethod() throws Exception {
        SetNewPasswordDTO dto = new SetNewPasswordDTO("token123", "newPassword");
        String payload = objectMapper.writeValueAsString(dto);

        mockMvc.perform(get("/api/auth/set-new-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Failure: Set New Password with Empty Body")
    public void setNewPasswordFailureEmptyBody() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/set-new-password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("Required request body " +
                "is missing"));
    }

    @Test
    @DisplayName("Failure: Set New Password with Invalid Token")
    public void setNewPasswordFailureInvalidToken() throws Exception {
        SetNewPasswordDTO dto = new SetNewPasswordDTO("invalid-token", "newPassword123");
        String payload = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/set-new-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("The token was expected " +
                "to have 3 parts, but got 1"));
    }

    @Test
    @DisplayName("Success: Set New Password")
    public void setNewPasswordSuccess() throws Exception {
        User user = new User("test@example.com", "oldPassword");
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecoverPasswordDTO(user.getUsername()))))
                .andExpect(status().isOk());

        PasswordRecoveryToken recoveryToken = passwordRecoveryTokenRepository.findByBelongsTo(user)
                .orElseThrow(() -> new AssertionError("Recovery token not found"));

        SetNewPasswordDTO dto = new SetNewPasswordDTO(recoveryToken.getPasswordRecoveryToken(),
                "newStrongPassword123!");
        String payload = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/set-new-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        assertFalse(passwordRecoveryTokenRepository.findByBelongsTo(user).isPresent());
    }
}
