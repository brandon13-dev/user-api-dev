package com.devsolutions.user_api.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

import com.devsolutions.user_api.dto.UserRequestDTO;
import com.devsolutions.user_api.dto.UserResponseDTO;
import com.devsolutions.user_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;


    // Test para validar que el usuario se registro con exito
    @Test
    public void registerUser_Success() throws Exception {
        UserRequestDTO request = new UserRequestDTO("test", "test@example.com", "pass123", "Name");
        UserResponseDTO response = new UserResponseDTO(1L, "test", "test@example.com", "Name", LocalDateTime.now());

        // Mockeamos el comportamiento del servicio
        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("test"));
    }

    // Test para validar el error cuando enviamos un email duplicado
    @Test
    public void registerUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // Creamos el primer usuario
        UserRequestDTO userRequest = new UserRequestDTO("test2", "test@example.com", "pass123", "Name");

        // Cuando reciba este request, lance una excepcion
        when(userService.createUser(any(UserRequestDTO.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: test@example.com"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Email already exists: test@example.com")));
    }

    // Test para validar el error cuando enviamos un username duplicado
    @Test
    public void registerUser_DuplicateUsername_ReturnsConflict() throws Exception {
        // Creamos la peticion con usuario duplicado
        UserRequestDTO userRequest = new UserRequestDTO("testUser", "test2@example.com", "pass123", "Name 2");

        // Cuando recibamos esta peticion, lanzamos la exepcion
        when(userService.createUser(any(UserRequestDTO.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists: " + userRequest.username()));
        
        // Ejecutamos y validamos
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Username already exists: testUser")));
    }

    // Test para validar que se muestran todos los users
    @Test
    public void getUsers_ShouldReturnList() throws Exception {
        // Preparamos los datos de prueba:
        List<UserResponseDTO> mockList = List.of(
        new UserResponseDTO(1L, "user1", "u1@mail.com", "Name 1", LocalDateTime.now()),
        new UserResponseDTO(2L, "user2", "u2@mail.com", "Name 2", LocalDateTime.now())
        );

        // Le decimos al servicio que devuelva una lista cuando lo llamen
        when(userService.getUsers()).thenReturn(mockList);

        // Ejecutamos y verificamos
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                // Verificamos que vengan 2
                .andExpect(jsonPath("$.length()").value(2))
                // Verificamos que el username del primer registro sea user1
                .andExpect(jsonPath("$[0].username").value("user1"))
                // Verificamos que el username del segundo registro sea user2
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    //Test para validar que se muestra un user segun su id
    @Test
    public void getUserById_ShouldReturnUser() throws Exception {
        // Preparamos los datos de prueba
        UserResponseDTO expectedUser = new UserResponseDTO(1L, "user1", "u1@mail.com", "Name 1", LocalDateTime.of(2026, 1, 1, 10, 0,0));

        // Le decimos al servicio que devuelva un usuario
        when(userService.getUser(1L)).thenReturn(expectedUser);

        // Ejecutamos y verificamos
        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                // Verificamos que el id del usuario sea el 1
                .andExpect(jsonPath("$.id").value(1L))
                // Verificamos que el username sea user1
                .andExpect(jsonPath("$.username").value("user1"))
                // Verificamos que el email sea u1@mail.com
                .andExpect(jsonPath("$.email").value("u1@mail.com"))
                // Verificamos que el full name sea Name 1
                .andExpect(jsonPath("$.fullName").value("Name 1"))
                // Verificamos la fecha
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T10:00:00"));
    }

    // Test para validar el error cuando no existe el usuario por id
    @Test
    public void getUserById_ShouldReturnNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by id 1"))
            .when(userService).getUser(1L);
        
        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found by id 1"));
        
        verify(userService).getUser(1L);
    }

    // Test para validar que se actualiza un registro
    @Test
    public void updateUserById_ShouldReturnUser() throws Exception {
        UserRequestDTO request = new UserRequestDTO("test", "test@example.com", "pass123", "Name");
        UserResponseDTO response = new UserResponseDTO(1L, "test", "test@example.com", "Name", LocalDateTime.of(2026, 1, 1, 10, 0,0));

        // Mockeamos el comportamiento del servicio
        when(userService.editUser(eq(1L), any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("test"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.fullName").value("Name"))
            .andExpect(jsonPath("$.createdAt").value("2026-01-01T10:00:00"));
        
    }

    // Test para validar que se elimina un registro
    @Test
    public void deleteUserById_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());
        
        verify(userService).deleteUser(1L);
    }

    // Test para validar que si no encuentra el user lanza not found
    @Test
    public void deleteUserById_ShouldReturnNotFound() throws Exception {

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by id 1"))
            .when(userService).deleteUser(1L);
        
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found by id 1"));

        verify(userService).deleteUser(1L);
    }
}
