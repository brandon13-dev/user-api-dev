package com.devsolutions.user_api.controller;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsolutions.user_api.dto.UserRequestDTO;
import com.devsolutions.user_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private UserRequestDTO validUser;

    @BeforeEach
    void setup(){
        // Limpiamos la base de datos antes de cada test
        userRepository.deleteAll();
        
        validUser = new UserRequestDTO(
            "testuser", 
            "test@example.com", 
            "password123", 
            "Test User"
        );
    }

    @Test
    void registerUser_Success() throws Exception{
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));              
    }

    @Test
    void registerUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // hacemos el registro del usuario
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated());
        
        // Intentamos registrar otro usuario con el mismo email
        UserRequestDTO duplicateEmail = new UserRequestDTO(
            "otheruser", 
            "test@example.com", 
            "otherpass", 
            "Other User"
        );

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmail)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Email already exists")));      
    }

    @Test
    void registerUser_DuplicateUsername_ReturnsConflict() throws Exception {
        // hacemos el registro del usuario
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated());

        // Creamos otro usuario con el mismo username
        UserRequestDTO duplicateUsername = new UserRequestDTO(
            "testuser", // mismo username 
            "different@example.com", 
            "otherpass", 
            "Other User"
        );
        
        // Intentamos registrar el usuario con el mismo username
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUsername)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Username already exists")));
    }

    @Test
    void registerUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Creamos un usuario con email invalido
        UserRequestDTO invalidEmail = new UserRequestDTO(
            "baduser", 
            "invalid-email", // email mal formado  
            "password123", 
            "Bad User"
        );

        // Intentamos registrar un usuario con email invalido
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_ShortPassword_ReturnsBadRequest() throws Exception{
        // Creamos un usuario con contrasena corta
        UserRequestDTO shortPassword = new UserRequestDTO(
            "weakuser",
            "weak@example.com",
            "123",  // Password muy corto (mínimo 6 caracteres)
            "Weak User"
        );

        // Intentamos registrar el usuario con contraseña corta
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPassword)))
                .andExpect(status().isBadRequest());
    }
}
