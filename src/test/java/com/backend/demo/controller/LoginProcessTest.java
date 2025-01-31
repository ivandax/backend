package com.backend.demo.controller;

import com.backend.demo.model.Organization;
import com.backend.demo.model.Role;
import com.backend.demo.model.User;
import com.backend.demo.repository.*;
import com.backend.demo.service.mailing.EmailService;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LoginProcessTest {

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
    private InvalidTokenRepository invalidTokenRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserVerificationTokenRepository userVerificationTokenRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    @BeforeEach
    void setup() {
        passwordRecoveryTokenRepository.deleteAll();
        invalidTokenRepository.deleteAll();
        userVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        organizationRepository.deleteAll();

        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                        .apply(springSecurity())
                        .build();

        Role dev = new Role("DEV");
        Role admin = new Role("ADMIN");
        roleRepository.saveAll(List.of(dev, admin));

        Organization org = new Organization("Main", 10);
        organizationRepository.save(org);

        User devUser = new User();
        devUser.addRole(dev);
        devUser.setUsername("dev@mail.com");
        devUser.setPassword("testPassword");
        devUser.setActive();
        devUser.setVerified(true);
        devUser.setOrganization(org);
        userRepository.save(devUser);
    }

    @Test
    @DisplayName("Login failure: login up with wrong method - GET")
    public void loginFailureByMethod() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "dev@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Login failure: User does not exist")
    public void loginFailureUserAlreadyExists() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "noExist@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("Invalid username or password"));
    }

    @Test
    @DisplayName("Login failure: Badly formatted email")
    public void loginFailureBadlyFormattedEmail() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "pizzaPizza")
                        .param("password", "testPassword"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("Invalid username or password"));
    }

    @Test
    @DisplayName("Login success: Login with username and password")
    public void loginSuccess() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "dev@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("access_token"));
        assertTrue(mvcResult.getResponse().getContentAsString().contains("refresh_token"));
    }

    @Test
    @DisplayName("Logout failure: Missing token")
    public void logoutFailureMissingToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "dev@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .header("authorization", ""))
                .andExpect(status().isBadRequest()).andReturn();

        assertTrue(logoutResult.getResponse().getContentAsString().contains("Token is missing"));
    }

    @Test
    @DisplayName("Logout success")
    public void logoutSuccess() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "dev@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        mockMvc.perform(post("/api/auth/logout")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

}
