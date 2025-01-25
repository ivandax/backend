package com.backend.demo.controller;

import com.backend.demo.dtos.TodoRequestDTO;
import com.backend.demo.model.Permission;
import com.backend.demo.model.Role;
import com.backend.demo.model.Todolist;
import com.backend.demo.model.User;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TodolistControllerTest {

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

    @BeforeEach
    void setup() {
        todolistRepository.deleteAll();
        invalidTokenRepository.deleteAll();
        userVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        organizationRepository.deleteAll();

        Permission permissionReadUsers = new Permission("read:users");
        Permission permissionCreateTodolist = new Permission("create:todolist");
        Permission permissionReadTodolist = new Permission("read:todolist");
        permissionRepository.saveAll(List.of(permissionReadUsers, permissionReadTodolist,
                permissionCreateTodolist));

        Role dev = new Role("DEV");
        Role admin = new Role("ADMIN");
        admin.setPermissions(new HashSet<>(List.of(permissionReadUsers, permissionCreateTodolist,
                permissionReadTodolist)));
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
    @DisplayName("Failure: Create todolists without auth")
    public void createTodolistsFailureNoAuth() throws Exception {
        mockMvc.perform(post("/api/todolists/create")).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @DisplayName("Failure: Create todolists without create:todolist permission")
    public void createTodolistsFailureNoPermissions() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
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

        mockMvc.perform(post("/api/todolists/create")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Failure: Create todolists with invalid body")
    public void createTodolistsFailureInvalidBody() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
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

        record CreateTodolistRequestMissingTodos(String title, String description) {
        }

        CreateTodolistRequestMissingTodos request =
                new CreateTodolistRequestMissingTodos("test", "some description");

        String payload = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(post("/api/todolists/create")
                        .header("authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest()).andReturn();


        assertTrue(mvcResult.getResponse().getContentAsString().contains("Could not validate " +
                "provided data. Please review data sent"));
    }

    @Test
    @DisplayName("Success: Create todolist")
    public void createTodolistSuccess() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
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

        record CreateTodolistRequest(String title, String description, List<TodoRequestDTO> todos) {
        }

        TodoRequestDTO todo = new TodoRequestDTO("Hello world! This is my todo", false);

        CreateTodolistRequest request =
                new CreateTodolistRequest("test", "some description", List.of(todo));

        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/todolists/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Failure: Get todolists without auth")
    public void getUsersFailureNoAuth() throws Exception {
        mockMvc.perform(get("/api/todolists")).andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @DisplayName("Failure: Get todolists without permissions")
    public void getUsersFailureNoPermissions() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
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
        mockMvc.perform(get("/api/todolists").header("authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @DisplayName("Success: get todolists for user")
    public void getTodolistsForUser() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
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
        mockMvc.perform(get("/api/todolists").header("authorization"
                        , "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andReturn();
    }

    @Test
    @DisplayName("Update a todolist failure: Invalid body")
    public void updateTodolistFailureInvalidBody() throws Exception {
        // Step 1: Authenticate as admin user
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        // Step 2: Create a todo list to update
        record CreateTodolistRequest(String title, String description, List<TodoRequestDTO> todos) {
        }

        TodoRequestDTO todo = new TodoRequestDTO("Initial todo", false);
        CreateTodolistRequest createRequest =
                new CreateTodolistRequest("Initial title", "Initial description", List.of(todo));

        String createPayload = objectMapper.writeValueAsString(createRequest);

        MvcResult createResult = mockMvc.perform(post("/api/todolists/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        Todolist createdTodolist =
                todolistRepository.findByTitle("Initial title").orElseThrow(() -> new RuntimeException("Todolist not found"));

        Integer todolistId = (Integer) createdTodolist.getId();

        // Step 3: Prepare the update payload
        record UpdateTodolistRequestMissingDescription(String title) {
        }

        TodoRequestDTO updatedTodo = new TodoRequestDTO("Updated todo", true);
        UpdateTodolistRequestMissingDescription updateRequest =
                new UpdateTodolistRequestMissingDescription("Updated title");

        String updatePayload = objectMapper.writeValueAsString(updateRequest);

        // Step 4: Perform the update request
        mockMvc.perform(post("/api/todolists/" + todolistId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Success: Update a todolist")
    public void updateTodolistSuccess() throws Exception {
        // Step 1: Authenticate as admin user
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        // Step 2: Create a todo list to update
        record CreateTodolistRequest(String title, String description, List<TodoRequestDTO> todos) {
        }

        TodoRequestDTO todo = new TodoRequestDTO("Initial todo", false);
        CreateTodolistRequest createRequest =
                new CreateTodolistRequest("Initial title", "Initial description", List.of(todo));

        String createPayload = objectMapper.writeValueAsString(createRequest);

        MvcResult createResult = mockMvc.perform(post("/api/todolists/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        Todolist createdTodolist =
                todolistRepository.findByTitle("Initial title").orElseThrow(() -> new RuntimeException("Todolist not found"));

        Integer todolistId = (Integer) createdTodolist.getId();

        // Step 3: Prepare the update payload
        record UpdateTodolistRequest(String title, String description) {
        }

        TodoRequestDTO updatedTodo = new TodoRequestDTO("Updated todo", true);
        UpdateTodolistRequest updateRequest =
                new UpdateTodolistRequest("Updated title", "Updated description");

        String updatePayload = objectMapper.writeValueAsString(updateRequest);

        // Step 4: Perform the update request
        mockMvc.perform(patch("/api/todolists/" + todolistId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }
}
