package com.devsolutions.user_api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.devsolutions.user_api.dto.UserRequestDTO;
import com.devsolutions.user_api.dto.UserResponseDTO;
import com.devsolutions.user_api.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;

    // Inyectamos por constructor
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDTO request){
        try {
            UserResponseDTO newUser = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                            .status(e.getStatusCode())
                            .body(Map.of("error", e.getReason()));
        }
        
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers(){
        List<UserResponseDTO> users = userService.getUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id){
        try {
            UserResponseDTO user = userService.getUser(id);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                            .status(e.getStatusCode())
                            .body(Map.of("error", e.getReason()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editUser(@PathVariable Long id, @Valid @RequestBody UserRequestDTO request){
        try {
            UserResponseDTO updatedUser = userService.editUser(id, request);
            return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Map.of("error", e.getReason()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        try {
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (ResponseStatusException e) {
            return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Map.of("error", e.getReason()));
        }
    }
}
