package com.backend.demo.controller;

import com.backend.demo.dtos.*;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.*;
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
        Permission permissionCreateTodolist = new Permission("create:todolist");
        Permission permissionReadTodolist = new Permission("read:todolist");
        Permission permissionUpdateTodolist = new Permission("update:todolist");
        permissionRepository.saveAll(List.of(permissionReadUsers,
                permissionReadTodolist,
                permissionCreateTodolist,
                permissionUpdateTodolist));

        Role dev = new Role("DEV");
        Role admin = new Role("ADMIN");
        admin.setPermissions(new HashSet<>(List.of(permissionReadUsers, permissionCreateTodolist,
                permissionReadTodolist, permissionUpdateTodolist)));
        roleRepository.saveAll(List.of(dev, admin));

        User adminUser = new User();
        adminUser.addRole(admin);
        adminUser.setUsername("admin@mail.com");
        adminUser.setPassword("testPassword");
        adminUser.setVerified(true);
        userRepository.save(adminUser);

        User adminUser2 = new User();
        adminUser2.addRole(admin);
        adminUser2.setUsername("admin2@mail.com");
        adminUser2.setPassword("testPassword");
        adminUser2.setVerified(true);
        userRepository.save(adminUser2);

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

        mockMvc.perform(post("/api/todolists/create")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Failure: Create todolists with invalid body")
    public void createTodolistsFailureInvalidBody() throws Exception {
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

        record CreateTodolistRequest(String title, String description, List<TodoRequestDTO> todos) {
        }

        TodoRequestDTO todo = new TodoRequestDTO("Hello world! This is my todo", false, 1);

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
        mockMvc.perform(get("/api/todolists").header("authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @DisplayName("Success: get todolists for user")
    public void getTodolistsForUser() throws Exception {
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
        mockMvc.perform(get("/api/todolists").header("authorization"
                        , "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andReturn();
    }

    @Test
    @DisplayName("Success: get todolists for user with sorting ASC")
    public void getTodolistsForUserWithSortingASC() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType
                                .APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        User user =
                userRepository.findByUsername("admin@mail.com").orElseThrow(() -> new RuntimeException("Todolist not found"));

        Todolist newTodoList1 = new Todolist(user);
        newTodoList1.setTitle("A: Should be first");
        Todolist newTodoList2 = new Todolist(user);
        newTodoList2.setTitle("B: Should be second");

        todolistRepository.saveAll(List.of(newTodoList1, newTodoList2));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");
        MvcResult result =
                mockMvc.perform(get("/api/todolists?sortBy=title&sortDirection=ASC").header(
                                "authorization"
                                , "Bearer " + accessToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items").isArray())
                        .andExpect(jsonPath("$.items", hasSize(2)))
                        .andReturn();

        ResourceResponseDTO<TodolistDTO> response =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        });

        List<TodolistDTO> todolists = response.getItems();
        assertEquals(2, todolists.size(), "Should have exactly 2 todolists in items array");
        TodolistDTO firstItem = todolists.get(0);
        TodolistDTO secondItem = todolists.get(1);
        assertEquals("A: Should be first", firstItem.getTitle());
        assertEquals("B: Should be second", secondItem.getTitle());
    }

    @Test
    @DisplayName("Update a todolist failure: Invalid body")
    public void updateTodolistFailureInvalidBody() throws Exception {
        // Step 1: Authenticate as admin user
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
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

        TodoRequestDTO todo = new TodoRequestDTO("Initial todo", false, 1);
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
        record UpdateTodolistRequestMissingItems(String title) {
        }

        TodoRequestDTO updatedTodo = new TodoRequestDTO("Updated todo", true, 1);
        UpdateTodolistRequestMissingItems updateRequest =
                new UpdateTodolistRequestMissingItems("Updated title");

        String updatePayload = objectMapper.writeValueAsString(updateRequest);

        // Step 4: Perform the update request
        mockMvc.perform(post("/api/todolists/" + todolistId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Failure: cannot update todolist without ownership")
    public void updateTodolistFailureByOwnership() throws Exception {
        User otherUser =
                userRepository.findByUsername("no_permissions@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist otherTodolist = new Todolist(otherUser);
        todolistRepository.save(otherTodolist);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        record UpdateTodolistRequest(String title, String description,
                                     List<TodoUpdateDTO> updateTodos,
                                     List<TodoRequestDTO> createTodos,
                                     List<String> deleteTodoIds) {
        }

        UpdateTodolistRequest updateRequest =
                new UpdateTodolistRequest("Updated title", "Updated description",
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        String updatePayload = objectMapper.writeValueAsString(updateRequest);

        MvcResult mvcResult = mockMvc.perform(patch("/api/todolists/" + otherTodolist.getId() +
                        "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest()).andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains("Error of ownership. " +
                "Does not belong to this todo list"));
    }

    @Test
    @DisplayName("Success: Update a todolist")
    public void updateTodolistSuccess() throws Exception {
        // Step 1: Authenticate as admin user
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
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

        TodoRequestDTO todo = new TodoRequestDTO("Initial todo", false, 1);
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
        record UpdateTodolistRequest(String title, String description,
                                     List<TodoUpdateDTO> updateTodos,
                                     List<TodoRequestDTO> createTodos,
                                     List<String> deleteTodoIds) {
        }

        UpdateTodolistRequest updateRequest =
                new UpdateTodolistRequest("Updated title", "Updated description",
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        String updatePayload = objectMapper.writeValueAsString(updateRequest);

        // Step 4: Perform the update request
        mockMvc.perform(patch("/api/todolists/" + todolistId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Success: Update a todolist with an update to a todo")
    public void updateExistingTodoSuccess() throws Exception {
        // Step 1: Authenticate as admin user
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        // Step 2: Create a todo list with one todo
        record CreateTodolistRequest(String title, String description, List<TodoRequestDTO> todos) {
        }

        TodoRequestDTO todo = new TodoRequestDTO("Initial todo", false, 1);
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

        Todo createdTodo =
                todoRepository.findByDescription("Initial todo").orElseThrow(() -> new RuntimeException("Todo not found"));
        Integer todoId = createdTodo.getId();
        Integer sequenceNumber = createdTodo.getSequenceNumber();

        // Step 3: Prepare the update payload (updating the existing todo)
        record UpdateTodolistRequest(String title, String description,
                                     List<TodoUpdateDTO> updateTodos,
                                     List<TodoRequestDTO> createTodos,
                                     List<String> deleteTodoIds) {
        }

        TodoUpdateDTO updatedTodo = new TodoUpdateDTO(todoId, "Updated todo description", true,
                sequenceNumber);
        UpdateTodolistRequest updateRequest =
                new UpdateTodolistRequest("Updated title", "Updated description",
                        List.of(updatedTodo), new ArrayList<>(), new ArrayList<>());

        String updatePayload = objectMapper.writeValueAsString(updateRequest);

        // Step 4: Perform the update request
        mockMvc.perform(patch("/api/todolists/" + createdTodolist.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Step 5: Validate that the todo was updated in the database
        Todolist updatedTodolistWithTodos =
                todolistRepository.findByIdWithTodos(createdTodolist.getId())
                        .orElseThrow(() -> new RuntimeException("Todolist not found"));

        Todo updatedTodoInDb = updatedTodolistWithTodos.getTodos().stream()
                .filter(t -> t.getId().equals(todoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Updated Todo not found"));

        assertEquals("Updated todo description", updatedTodoInDb.getDescription());
        assertTrue(updatedTodoInDb.isCompleted());
    }

    @Test
    @DisplayName("Success: Add a new todo to an existing todolist")
    public void addNewTodoSuccess() throws Exception {
        // Step 1: Authenticate as admin user
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        // Step 2: Create a todolist with no todos
        record CreateTodolistRequest(String title, String description, List<TodoRequestDTO> todos) {
        }

        CreateTodolistRequest createRequest =
                new CreateTodolistRequest("Todo List for Adding", "Testing adding todos",
                        new ArrayList<>());

        String createPayload = objectMapper.writeValueAsString(createRequest);

        MvcResult createResult = mockMvc.perform(post("/api/todolists/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        Todolist createdTodolist =
                todolistRepository.findByTitle("Todo List for Adding").orElseThrow(() -> new RuntimeException("Todolist not found"));

        // Step 3: Prepare the update payload (creating a new todo)
        record UpdateTodolistRequest(String title, String description,
                                     List<TodoUpdateDTO> updateTodos,
                                     List<TodoRequestDTO> createTodos,
                                     List<String> deleteTodoIds) {
        }

        TodoRequestDTO newTodo = new TodoRequestDTO("Newly added todo", false, 1);
        UpdateTodolistRequest updateRequest =
                new UpdateTodolistRequest("Todo List for Adding", "Testing adding todos",
                        new ArrayList<>(), List.of(newTodo), new ArrayList<>());

        String updatePayload = objectMapper.writeValueAsString(updateRequest);

        // Step 4: Perform the update request (adding the new todo)
        mockMvc.perform(patch("/api/todolists/" + createdTodolist.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Step 5: Validate that the new todo was created in the database
        Todolist updatedTodolistWithTodos =
                todolistRepository.findByIdWithTodos(createdTodolist.getId())
                        .orElseThrow(() -> new RuntimeException("Todolist not found"));

        assertEquals(1, updatedTodolistWithTodos.getTodos().size());
        assertEquals("Newly added todo",
                updatedTodolistWithTodos.getTodos().get(0).getDescription());
    }

    @Test
    @DisplayName("Success: Delete an existing todo from a todolist")
    public void deleteTodoSuccess() throws Exception {
        // Step 1: Authenticate as admin user
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        // Step 2: Create a todo list with one todo
        record CreateTodolistRequest(String title, String description, List<TodoRequestDTO> todos) {
        }

        TodoRequestDTO todo = new TodoRequestDTO("Todo to delete", false, 1);
        CreateTodolistRequest createRequest =
                new CreateTodolistRequest("Todo List for Deletion", "Testing deleting todos",
                        List.of(todo));

        String createPayload = objectMapper.writeValueAsString(createRequest);

        MvcResult createResult = mockMvc.perform(post("/api/todolists/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        Todolist createdTodolist =
                todolistRepository.findByTitle("Todo List for Deletion").orElseThrow(() -> new RuntimeException("Todolist not found"));

        Todo createdTodo =
                todoRepository.findByDescription("Todo to delete").orElseThrow(() -> new RuntimeException("Todo not found"));
        Integer todoId = createdTodo.getId();

        // Step 3: Prepare the update payload (deleting the existing todo)
        record UpdateTodolistRequest(String title, String description,
                                     List<TodoUpdateDTO> updateTodos,
                                     List<TodoRequestDTO> createTodos,
                                     List<String> deleteTodoIds) {
        }

        UpdateTodolistRequest updateRequest =
                new UpdateTodolistRequest("Todo List for Deletion", "Testing deleting todos",
                        new ArrayList<>(), new ArrayList<>(), List.of(todoId.toString()));

        String updatePayload = objectMapper.writeValueAsString(updateRequest);

        // Step 4: Perform the update request (deleting the todo)
        mockMvc.perform(patch("/api/todolists/" + createdTodolist.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload)
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Step 5: Validate that the todo was deleted from the database
        Todolist updatedTodolistWithTodos =
                todolistRepository.findByIdWithTodos(createdTodolist.getId())
                        .orElseThrow(() -> new RuntimeException("Todolist not found"));

        assertTrue(updatedTodolistWithTodos.getTodos().isEmpty());
    }

    @Test
    @DisplayName("Failure: Create todolist with invalid body")
    public void addTodoFailureInvalidBody() throws Exception {
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

        User user =
                userRepository.findByUsername("admin@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist todolist = new Todolist(user);
        todolistRepository.save(todolist);

        record CreateTodoInvalidBody(String title) {
        }

        CreateTodoInvalidBody request = new CreateTodoInvalidBody("test");
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/todolists/" + todolist.getId() + "/add-todo")
                        .header("authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("Could not validate provided data")));
    }

    @Test
    @DisplayName("Failure: Create todolist by ownership")
    public void addTodoFailureByOwnership() throws Exception {
        User otherUser =
                userRepository.findByUsername("no_permissions@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist otherTodolist = new Todolist(otherUser);
        todolistRepository.save(otherTodolist);

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

        record CreateTodoBody(String description, boolean isCompleted, Integer sequenceNumber) {
        }

        CreateTodoBody request = new CreateTodoBody("test", false, 1);
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/todolists/" + otherTodolist.getId() + "/add-todo")
                        .header("authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("Error of ownership. " +
                        "Does not belong to this todo list")));
    }

    @Test
    @DisplayName("Success: Add todo")
    public void addTodoSuccess() throws Exception {
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

        User user =
                userRepository.findByUsername("admin@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist todolist = new Todolist(user);
        todolistRepository.save(todolist);

        record CreateTodoBody(String description, boolean isCompleted, Integer sequenceNumber) {
        }

        CreateTodoBody request = new CreateTodoBody("test", false, 1);
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/todolists/" + todolist.getId() + "/add-todo")
                        .header("authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Failure: Update todo by ownership")
    public void updateTodoFailureByOwnership() throws Exception {
        User otherUser =
                userRepository.findByUsername("no_permissions@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist otherTodolist = new Todolist(otherUser);
        Todo otherTodo = new Todo("test", otherTodolist, 1);
        List<Todo> todos = List.of(otherTodo);
        otherTodolist.setTodos(todos);
        todolistRepository.save(otherTodolist);

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

        record UpdateTodoBody(String description, boolean isCompleted, Integer sequenceNumber) {
        }

        UpdateTodoBody request = new UpdateTodoBody("test", false, 1);
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/todolists/" + otherTodolist.getId() + "/todos/" + otherTodo.getId())
                        .header("authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("Error of ownership. " +
                        "Does not belong to this todo list")));
    }

    @Test
    @DisplayName("Failure: Update todo invalid body")
    public void updateTodoFailureInvalidBody() throws Exception {
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

        User user =
                userRepository.findByUsername("admin@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist todolist = new Todolist(user);
        Todo todo = new Todo("test", todolist, 1);
        List<Todo> todos = List.of(todo);
        todolist.setTodos(todos);
        todolistRepository.save(todolist);

        record CreateTodoInvalidBody(String invalidProperty) {
        }

        CreateTodoInvalidBody request = new CreateTodoInvalidBody("test");
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/todolists/" + todolist.getId() + "/todos/" + todo.getId())
                        .header("authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("Could not validate provided data")));
    }

    @Test
    @DisplayName("Success Update todo")
    public void successUpdateTodo() throws Exception {
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

        User user =
                userRepository.findByUsername("admin@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist todolist = new Todolist(user);
        Todo todo = new Todo("test", todolist, 1);
        List<Todo> todos = List.of(todo);
        todolist.setTodos(todos);
        todolistRepository.save(todolist);

        record CreateTodoBody(String description, boolean isCompleted, Integer sequenceNumber) {
        }

        CreateTodoBody request = new CreateTodoBody("test", false, 1);
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/todolists/" + todolist.getId() + "/todos/" + todo.getId())
                        .header("authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Failure: Delete todolist without auth")
    public void deleteTodolistFailureNoAuth() throws Exception {
        mockMvc.perform(delete("/api/todolists/1/delete"))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("Failure: Delete todolist without permissions")
    public void deleteTodolistFailureNoPermissions() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "no_permissions@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> tokensResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokensResponse.get("access_token");

        mockMvc.perform(delete("/api/todolists/1/delete")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("Failure: Delete todolist that doesn't exist")
    public void deleteTodolistFailureNotFound() throws Exception {
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

        // Use a non-existent ID
        mockMvc.perform(delete("/api/todolists/999/delete")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains("Todolist not found")));
    }

    @Test
    @DisplayName("Failure: Delete todolist without ownership")
    public void deleteTodolistFailureByOwnership() throws Exception {
        User otherUser =
                userRepository.findByUsername("no_permissions@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist otherTodolist = new Todolist(otherUser);
        todolistRepository.save(otherTodolist);

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

        mockMvc.perform(delete("/api/todolists/" + otherTodolist.getId() + "/delete")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString()
                        .contains("Error of ownership. You don't have permission to delete this " +
                                "todolist")));
    }

    @Test
    @DisplayName("Success: Delete todolist")
    public void deleteTodolistSuccess() throws Exception {
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

        User user =
                userRepository.findByUsername("admin@mail.com").orElseThrow(() -> new RuntimeException("User not found"));

        Todolist todolist = new Todolist(user);
        todolist.setTitle("Todolist to delete");
        // Add a todo to test cascade delete
        Todo todo = new Todo("Test todo", todolist, 1);
        List<Todo> todos = List.of(todo);
        todolist.setTodos(todos);
        todolistRepository.save(todolist);

        // Verify todolist exists before deletion
        assertTrue(todolistRepository.findById(todolist.getId()).isPresent());

        mockMvc.perform(delete("/api/todolists/" + todolist.getId() + "/delete")
                        .header("authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // Verify todolist no longer exists after deletion
        assertTrue(todolistRepository.findById(todolist.getId()).isEmpty());
        // Verify associated todo was also deleted (cascade)
        assertTrue(todoRepository.findById(todo.getId()).isEmpty());
    }

    @Test
    @DisplayName("Failure: Cannot add collaborator without permission")
    public void addCollaboratorNoPermission() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Get the owner user
        User owner = userRepository.findByUsername("admin2@mail.com")
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        // Create a todolist as the owner
        Todolist todolist = new Todolist(owner);
        todolist.setTitle("Private Todolist");
        todolistRepository.save(todolist);

        // Login as another user (non-owner) trying to add collaborator
        MvcResult otherLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> otherTokens =
                objectMapper.readValue(otherLoginResult.getResponse().getContentAsString(),
                        Map.class);
        String otherAccessToken = otherTokens.get("access_token");

        // Find the user to be added as a collaborator (must be different from "other")
        User collaborator = userRepository.findByUsername("no_permissions@mail.com")
                .orElseThrow(() -> new RuntimeException("Collaborator user not found"));

        record AddCollaboratorBody(Integer collaboratorId) {
        }

        AddCollaboratorBody request = new AddCollaboratorBody(collaborator.getUserId());
        String payload = objectMapper.writeValueAsString(request);

        // Perform the request as a non-owner user
        mockMvc.perform(post("/api/todolists/" + todolist.getId() + "/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + otherAccessToken)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse()
                        .getContentAsString()
                        .contains("You don't have permission to share this todolist")));
    }

    @Test
    @DisplayName("Failure: User to add as collaborator not found")
    public void addCollaboratorUserNotFound() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Login as owner who will share the todolist
        MvcResult ownerLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> ownerTokens =
                objectMapper.readValue(ownerLoginResult.getResponse().getContentAsString(), Map.class);
        String ownerAccessToken = ownerTokens.get("access_token");

        // Get the user object of the owner
        User owner = userRepository.findByUsername("admin@mail.com")
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        // Create a todolist for this user
        Todolist todolist = new Todolist(owner);
        todolist.setTitle("Todolist for invalid collaborator");
        todolistRepository.save(todolist);

        // Use a collaborator ID that doesn't exist
        int invalidCollaboratorId = 999999; // Assumes this ID doesn't exist

        // Prepare request body as record
        record AddCollaboratorBody(Integer collaboratorId) {}
        AddCollaboratorBody request = new AddCollaboratorBody(invalidCollaboratorId);
        String payload = objectMapper.writeValueAsString(request);

        // Perform the request as the owner
        mockMvc.perform(post("/api/todolists/" + todolist.getId() + "/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerAccessToken)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse()
                        .getContentAsString()
                        .contains("User to add not found")));
    }

    @Test
    @DisplayName("Failure: Cannot add yourself as a collaborator")
    public void addCollaboratorSelfNotAllowed() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Login as the user who will own the todolist and try to add themselves
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> tokens =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String accessToken = tokens.get("access_token");

        // Get the user
        User user = userRepository.findByUsername("admin@mail.com")
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create a todolist owned by this user
        Todolist todolist = new Todolist(user);
        todolist.setTitle("Self-collaborator test");
        todolistRepository.save(todolist);

        // Attempt to add self as collaborator
        record AddCollaboratorBody(Integer collaboratorId) {}
        AddCollaboratorBody request = new AddCollaboratorBody(user.getUserId());
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/todolists/" + todolist.getId() + "/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse()
                        .getContentAsString()
                        .contains("You cannot add yourself as a collaborator")));
    }

    @Test
    @DisplayName("Failure: User is already a collaborator")
    public void addCollaboratorAlreadyExists() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Login as the owner of the todolist
        MvcResult ownerLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> ownerTokens =
                objectMapper.readValue(ownerLoginResult.getResponse().getContentAsString(), Map.class);
        String ownerAccessToken = ownerTokens.get("access_token");

        // Get owner user
        User owner = userRepository.findByUsername("admin@mail.com")
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        // Get another user to add as a collaborator
        User collaborator = userRepository.findByUsername("no_permissions@mail.com")
                .orElseThrow(() -> new RuntimeException("Collaborator user not found"));

        // Create a todolist and manually add collaborator
        Todolist todolist = new Todolist(owner);
        todolist.setTitle("Todolist with duplicate collaborator");
        todolist.getSharedWith().add(collaborator); // Manually add collaborator
        todolistRepository.save(todolist);

        // Attempt to add the same collaborator again
        record AddCollaboratorBody(Integer collaboratorId) {}
        AddCollaboratorBody request = new AddCollaboratorBody(collaborator.getUserId());
        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/todolists/" + todolist.getId() + "/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerAccessToken)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse()
                        .getContentAsString()
                        .contains("This user is already a collaborator")));
    }

    @Test
    @DisplayName("Success: Add collaborator to todolist")
    public void addCollaboratorSuccess() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Login as the owner
        MvcResult ownerLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("username", "admin@mail.com")
                        .param("password", "testPassword"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> ownerTokens =
                objectMapper.readValue(ownerLoginResult.getResponse().getContentAsString(), Map.class);
        String ownerAccessToken = ownerTokens.get("access_token");

        // Get owner user
        User owner = userRepository.findByUsername("admin@mail.com")
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        // Get collaborator user
        User collaborator = userRepository.findByUsername("no_permissions@mail.com")
                .orElseThrow(() -> new RuntimeException("Collaborator user not found"));

        // Create a todolist as the owner
        Todolist todolist = new Todolist(owner);
        todolist.setTitle("Shareable Todolist");
        todolistRepository.save(todolist);

        // Make sure the collaborator is not already added
        assertFalse(todolist.getSharedWith().contains(collaborator));

        // Prepare request payload
        record AddCollaboratorBody(Integer collaboratorId) {}
        AddCollaboratorBody request = new AddCollaboratorBody(collaborator.getUserId());
        String payload = objectMapper.writeValueAsString(request);

        // Send the request to add the collaborator
        mockMvc.perform(post("/api/todolists/" + todolist.getId() + "/add-collaborator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerAccessToken)
                        .content(payload))
                .andExpect(status().isOk());
    }
}
