package com.backend.demo.controller;

import com.backend.demo.repository.UserRepository;
import com.backend.demo.service.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        User devUser = new User();
        devUser.setUsername("dev@mail.com");
        devUser.setPassword("test");

        userRepository.saveAll(List.of(devUser));
    }

    @Test
    @DisplayName("Failure: Sign up with wrong method - GET")
    public void signUpFailureByMethod() throws Exception {

        record SignUpRequest(String username, String password) {}

        SignUpRequest request = new SignUpRequest("test@mail.com", "test1234");

        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(get("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    @DisplayName("Failure: Sign up with empty body")
    public void signUpFailureBEmptyBody() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(1, userRepository.findAll().size());
        assertTrue(mvcResult.getResponse().getContentAsString().contains("Required request body is missing"));
    }

    @Test
    @DisplayName("Success: Sign up with username and password")
    public void signUpSuccess() throws Exception {

        record SignUpRequest(String username, String password) {}

        SignUpRequest request = new SignUpRequest("test@mail.com", "test1234");

        String payload = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals(2, userRepository.findAll().size());
    }

    @Test
    @DisplayName("Failure: User already exists")
    public void signUpFailureOnExistingUser() throws Exception {

        record SignUpRequest(String username, String password) {}

        SignUpRequest request = new SignUpRequest("dev@mail.com", "test1234");

        String payload = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(1, userRepository.findAll().size());
        assertTrue(mvcResult.getResponse().getContentAsString().contains("Username already exists"));
    }

    @Test
    @DisplayName("Failure: Badly formatted email")
    public void signUpFailureOnBadEmailAddress() throws Exception {

        record SignUpRequest(String username, String password) {}

        SignUpRequest request = new SignUpRequest("test", "test1234");

        String payload = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(1, userRepository.findAll().size());
        assertTrue(mvcResult.getResponse().getContentAsString().contains("Could not validate provided data. Please review data sent"));
        assertTrue(mvcResult.getResponse().getContentAsString().contains("must be a well-formed email address"));
    }

}
