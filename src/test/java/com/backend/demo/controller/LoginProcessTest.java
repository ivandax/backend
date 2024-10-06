package com.backend.demo.controller;

import com.backend.demo.model.Organization;
import com.backend.demo.model.Role;
import com.backend.demo.model.User;
import com.backend.demo.repository.OrganizationRepository;
import com.backend.demo.repository.RoleRepository;
import com.backend.demo.repository.UserRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
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
        devUser.setOrganization(org);
        userRepository.save(devUser);
    }

    @Test
    @DisplayName("Login failure: login up with wrong method - GET")
    public void loginFailureByMethod() throws Exception {
        mockMvc.perform(get("/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "dev@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("Login failure: User does not exist")
    public void loginFailureUserAlreadyExists() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "dev@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("Bad credentials"));
    }

    @Test
    @DisplayName("Login success: Login with username and password")
    public void loginSuccess() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "dev@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("access_token"));
        assertTrue(mvcResult.getResponse().getContentAsString().contains("refresh_token"));
    }

}
