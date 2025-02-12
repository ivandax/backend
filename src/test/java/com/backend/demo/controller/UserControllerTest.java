package com.backend.demo.controller;

import com.backend.demo.dtos.ResourceResponseDTO;
import com.backend.demo.dtos.User.UserResponseDTO;
import com.backend.demo.model.*;
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
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
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

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    @BeforeEach
    void setup() {
        todoRepository.deleteAll();
        todolistRepository.deleteAll();
        passwordRecoveryTokenRepository.deleteAll();
        invalidTokenRepository.deleteAll();
        userVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        organizationRepository.deleteAll();

        Permission permissionReadUsers = new Permission("read:users");
        Permission permissionReadSelfUser = new Permission("read:self-user");
        permissionRepository.saveAll(List.of(permissionReadUsers, permissionReadSelfUser));

        Role dev = new Role("DEV");
        Role admin = new Role("ADMIN");
        admin.setPermissions(new HashSet<>(List.of(permissionReadUsers, permissionReadSelfUser)));
        roleRepository.saveAll(List.of(dev, admin));

        User adminUser = new User();
        adminUser.addRole(admin);
        adminUser.setUsername("admin@mail.com");
        adminUser.setPassword("testPassword");
        adminUser.setVerified(true);
        userRepository.save(adminUser);

        User noRoleUser = new User();
        noRoleUser.setUsername("no_permissions@mail.com");
        noRoleUser.setPassword("testPassword");
        noRoleUser.setVerified(true);
        userRepository.save(noRoleUser);
    }

    @Test
    @DisplayName("Failure: Get users without auth")
    public void getUsersFailureNoAuth() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @DisplayName("Failure: Get users without read:users permission")
    public void getUsersFailureNoAdminRole() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "no_permissions@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        mockMvc.perform(get("/api/users")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Success: Get users")
    public void getUsersSuccess() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");


        MvcResult usersResult = mockMvc.perform(get("/api/users")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        ResourceResponseDTO<UserResponseDTO> response =
                objectMapper.readValue(usersResult.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        });

        List<UserResponseDTO> users = response.getItems();

        assertTrue(usersResult.getResponse().getContentAsString().contains("admin@mail.com"));
        assertTrue(usersResult.getResponse().getContentAsString().contains("no_permissions@mail.com"));
        assertEquals(2, users.size(), "Should have exactly 2 users in items array");
    }

    @Test
    @DisplayName("Success: Get users with Sorting by username DESC")
    public void getUsersSuccessWithSortingDESC() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");


        MvcResult usersResult = mockMvc.perform(get("/api/users?sortBy=username&sortDirection=DESC")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(usersResult.getResponse().getContentAsString());

        ResourceResponseDTO<UserResponseDTO> response =
                objectMapper.readValue(usersResult.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        });

        List<UserResponseDTO> users = response.getItems();

        assertTrue(usersResult.getResponse().getContentAsString().contains("admin@mail.com"));
        assertTrue(usersResult.getResponse().getContentAsString().contains("no_permissions@mail.com"));
        assertEquals(2, users.size(), "Should have exactly 2 users in items array");
        UserResponseDTO firstItem = users.get(0);
        UserResponseDTO secondItem = users.get(1);
        assertEquals("no_permissions@mail.com", firstItem.getUsername());
        assertEquals("admin@mail.com", secondItem.getUsername());
    }

    @Test
    @DisplayName("Success: Get users with Sorting by username ASC")
    public void getUsersSuccessWithSortingASC() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");


        MvcResult usersResult = mockMvc.perform(get("/api/users?sortBy=username&sortDirection=ASC")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(usersResult.getResponse().getContentAsString());

        ResourceResponseDTO<UserResponseDTO> response =
                objectMapper.readValue(usersResult.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        });

        List<UserResponseDTO> users = response.getItems();

        assertTrue(usersResult.getResponse().getContentAsString().contains("admin@mail.com"));
        assertTrue(usersResult.getResponse().getContentAsString().contains("no_permissions@mail.com"));
        assertEquals(2, users.size(), "Should have exactly 2 users in items array");
        UserResponseDTO firstItem = users.get(0);
        UserResponseDTO secondItem = users.get(1);
        assertEquals("admin@mail.com", firstItem.getUsername());
        assertEquals("no_permissions@mail.com", secondItem.getUsername());
    }

    @Test
    @DisplayName("Failure: Get self user without auth")
    public void getSelfUserNoAuth() throws Exception {
        mockMvc.perform(get("/api/users/logged-in-user"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @DisplayName("Failure: Get self user without read:self-user permission")
    public void getSelfUserNoAdminRole() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "no_permissions@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        mockMvc.perform(get("/api/users/logged-in-user")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Success: Get self")
    public void getSelfUserSuccess() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");


        MvcResult userResult = mockMvc.perform(get("/api/users/logged-in-user")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(userResult.getResponse().getContentAsString().contains("admin@mail.com"));
    }
}
