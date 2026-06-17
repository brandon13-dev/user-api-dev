package com.devsolutions.user_api.service;

import java.util.List;

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
    public UserResponseDTO createUser(UserRequestDTO request) {
        // Validamos que no exista el email (Con una mejor excepcion)
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + request.email());
        }

        // Validamos que no exista el username (Con una mejor excepcion)
        if (userRepository.existsByUsername(request.username())) {
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

    // Metodo para listar todos los usuarios
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsers() {
        List<User> users = userRepository.findAll();

        List<UserResponseDTO> usersDTO = users
                .stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getCreatedAt()))
                .toList();

        return usersDTO;
    }

    // Metodo para mostrar un usuario por id
    @Transactional(readOnly = true)
    public UserResponseDTO getUser(Long id) {
        // Verificamos que exista el usuario por id
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found by id " + id));

        return convertToResponseDTO(user);
    }

    // Metodo para editar un usuario
    @Transactional
    public UserResponseDTO editUser(Long id, UserRequestDTO request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found by id " + id));

        // Valida solo si el email cambio
        if (!user.getEmail().equals(request.email())) {
            // Verificamos que el email que viene del request no este ya en la base de datos
            if (userRepository.existsByEmail(request.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + request.email());
            }
        }

        // Valida solo si el username cambio
        if (!user.getUsername().equals(request.username())) {
            // Validamos que no exista el username (Con una mejor excepcion)
            if (userRepository.existsByUsername(request.username())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Username already exists: " + request.username());
            }
        }

        // Actualizamos los campos del user ya existente
        user.setUsername(request.username());
        user.setEmail(request.email());

        // Encriptamos la contraseña si no cambio
        if (user.getPassword() != request.password()) {
            String encryptedPassword = passwordEncoder.encode(request.password());
            user.setPassword(encryptedPassword);
        }

        user.setFullName(request.fullName());
        User updateUser = userRepository.save(user);
        return convertToResponseDTO(updateUser);
    }

    // Metodo para eliminar un usuario por id
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User not found by id " + id
            ));
        
        userRepository.delete(user);
    }

    // Metodo para convertir de User a UserResponseDTO
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO userDTO = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt());

        return userDTO;
    }
}
