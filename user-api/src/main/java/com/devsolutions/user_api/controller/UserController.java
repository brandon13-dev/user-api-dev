package com.devsolutions.user_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO request){
        UserResponseDTO newUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }
}
