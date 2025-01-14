package com.backend.demo.controller;

import com.backend.demo.model.Organization;
import com.backend.demo.model.Role;
import com.backend.demo.model.User;
import com.backend.demo.model.UserVerificationToken;
import com.backend.demo.repository.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

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
    @DisplayName("Success: Get users")
    public void getUsersSuccess() throws Exception {
        MvcResult usersResult =
                mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andReturn();

        assertTrue(usersResult.getResponse().getContentAsString().contains("dev@mail.com"));
    }
}
