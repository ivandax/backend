package com.backend.demo.controller;

import com.backend.demo.model.Organization;
import com.backend.demo.model.Role;
import com.backend.demo.model.UserVerificationToken;
import com.backend.demo.repository.OrganizationRepository;
import com.backend.demo.repository.RoleRepository;
import com.backend.demo.repository.UserRepository;
import com.backend.demo.model.User;
import com.backend.demo.repository.UserVerificationTokenRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
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
    private RoleRepository roleRepository;

    @BeforeEach
    void setup() {
        userVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        organizationRepository.deleteAll();

        Role dev = new Role("DEV");
        Role admin = new Role("ADMIN");
        roleRepository.saveAll(List.of(dev, admin));

        Organization org = new Organization("Main", 10);
        organizationRepository.save(org);

        User devUser = new User();
        devUser.addRole(dev);
        devUser.setUsername("dev@mail.com");
        devUser.setPassword("testPassword");
        devUser.setOrganization(org);
        userRepository.save(devUser);
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

        assertEquals(2, userRepository.findAll().size());
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

}
