package com.devsolutions.user_api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devsolutions.user_api.dto.UserRequestDTO;
import com.devsolutions.user_api.dto.UserResponseDTO;
import com.devsolutions.user_api.entity.User;
import com.devsolutions.user_api.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inyectamos por constructor
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request){
        // Validamos que no exista el email (Con una mejor excepcion)
        if (userRepository.existsByEmail(request.email())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + request.email());
        }

        // Validamos que no exista el username (Con una mejor excepcion)
        if (userRepository.existsByUsername(request.username())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists: " + request.username());
        } 

        // Creamos la entidad
        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());

        // Encriptamos la contraseña
        String encryptedPassword = passwordEncoder.encode(request.password());
        newUser.setPassword(encryptedPassword);

        newUser.setFullName(request.fullName());

        // Guardar
        User savedUser = userRepository.save(newUser);

        // Convertimos a DTO de respuesta
        return convertToResponseDTO(savedUser);
    }

    // Metodo para convertir de User a UserResponseDTO
    private UserResponseDTO convertToResponseDTO(User user){
        UserResponseDTO userDTO = new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getCreatedAt()
        );

        return userDTO;
    }
}
