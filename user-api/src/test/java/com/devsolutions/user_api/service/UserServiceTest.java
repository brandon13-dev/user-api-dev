package com.devsolutions.user_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.devsolutions.user_api.dto.UserRequestDTO;
import com.devsolutions.user_api.entity.User;
import com.devsolutions.user_api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequestDTO validRequest;

    @BeforeEach
    void setup(){
        validRequest = new UserRequestDTO(
            "serviceuser", 
            "service@example.com", 
            "password123", 
            "Service User");
    }
    
    @Test
    void createUser_Success(){
        // Arange
        when(userRepository.existsByEmail(validRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(validRequest.username())).thenReturn(false);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(validRequest.username());
        savedUser.setEmail(validRequest.email());
        savedUser.setFullName(validRequest.fullName());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        var response = userService.createUser(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("serviceuser", response.username());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException(){
        // Arange
        when(userRepository.existsByEmail(validRequest.email())).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(validRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_duplicateUsername_ThrowsException() {
        // Arange
        when(userRepository.existsByUsername(validRequest.username())).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(validRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}
