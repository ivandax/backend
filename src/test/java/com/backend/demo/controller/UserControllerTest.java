package com.backend.demo.controller;

import com.backend.demo.dtos.ResourceResponseDTO;
import com.backend.demo.dtos.User.UserResponseDTO;
import com.backend.demo.model.*;
import com.backend.demo.repository.*;
import com.backend.demo.service.mailing.ResendEmailService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    private ResendEmailService resendEmailService;

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

    @Autowired
    private UserCommonCollaboratorsRepository userCommonCollaboratorsRepository;

    @BeforeEach
    void setup() {
        todoRepository.deleteAll();
        todolistRepository.deleteAll();
        passwordRecoveryTokenRepository.deleteAll();
        invalidTokenRepository.deleteAll();
        userVerificationTokenRepository.deleteAll();
        userCommonCollaboratorsRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        organizationRepository.deleteAll();

        Permission permissionReadUsers = new Permission("read:users");
        Permission permissionUpdateUsers = new Permission("update:users");
        Permission permissionReadSelfUser = new Permission("read:self-user");
        permissionRepository.saveAll(List.of(permissionReadUsers, permissionReadSelfUser,
                permissionUpdateUsers));

        Role dev = new Role("DEV");
        Role admin = new Role("ADMIN");
        admin.setPermissions(new HashSet<>(List.of(permissionReadUsers, permissionReadSelfUser,
                permissionUpdateUsers)));
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
        assertTrue(usersResult.getResponse().getContentAsString().contains("no_permissions@mail" +
                ".com"));
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
        assertTrue(usersResult.getResponse().getContentAsString().contains("no_permissions@mail" +
                ".com"));
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
        assertTrue(usersResult.getResponse().getContentAsString().contains("no_permissions@mail" +
                ".com"));
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

    @Test
    void shouldReturnUserByUsername() throws Exception {
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

        String username = "admin@mail.com";
        mockMvc.perform(get("/api/users/by-username/{username}", username).header("authorization"
                                , "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
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

        mockMvc.perform(get("/api/users/by-username/{username}", "nonexistentuser").header(
                        "authorization"
                        , "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Fail: Cannot add yourself as collaborator")
    public void addCollaboratorCannotAddYourself() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        MvcResult userResult = mockMvc.perform(get("/api/users/by-username/{username}", "admin" +
                        "@mail.com")
                        .header("authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> userData =
                objectMapper.readValue(userResult.getResponse().getContentAsString(), Map.class);
        Integer userId = ((Number) userData.get("userId")).intValue();

        record AddCollaboratorRequest(Integer collaboratorId) {
        }

        AddCollaboratorRequest request = new AddCollaboratorRequest(userId);
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/users/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains("A user cannot add themselves as a collaborator")));
    }

    @Test
    @DisplayName("Fail: Cannot add the same collaborator twice")
    public void addCollaboratorDuplicate() throws Exception {
        // Step 1: Login as admin
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        // Step 2: Get user2's ID
        MvcResult userResult = mockMvc.perform(get("/api/users/by-username/{username}", "no_permissions@mail.com")
                        .header("authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> userData =
                objectMapper.readValue(userResult.getResponse().getContentAsString(), Map.class);
        Integer userId = ((Number) userData.get("userId")).intValue();

        record AddCollaboratorRequest(Integer collaboratorId) {
        }

        AddCollaboratorRequest request = new AddCollaboratorRequest(userId);
        String payload = objectMapper.writeValueAsString(request);

        // Step 3: First call should succeed
        mockMvc.perform(post("/api/users/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Step 4: Second call should fail with a meaningful message
        mockMvc.perform(post("/api/users/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains("This collaborator relationship already exists")));
    }

    @Test
    @DisplayName("Success: Add collaborator")
    public void addCollaboratorSuccess() throws Exception {
        // Step 1: Login as admin
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        // Step 2: Get user2's ID
        MvcResult userResult = mockMvc.perform(get("/api/users/by-username/{username}", "no_permissions@mail.com")
                        .header("authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> userData =
                objectMapper.readValue(userResult.getResponse().getContentAsString(), Map.class);
        Integer userId = ((Number) userData.get("userId")).intValue();

        record AddCollaboratorRequest(Integer collaboratorId) {
        }

        AddCollaboratorRequest request = new AddCollaboratorRequest(userId);
        String payload = objectMapper.writeValueAsString(request);

        // Step 3: First call should succeed
        mockMvc.perform(post("/api/users/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }
}
