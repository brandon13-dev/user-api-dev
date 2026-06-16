package com.devsolutions.user_api.dto;

import java.time.LocalDateTime;

public record UserResponseDTO(
    Long id,
    String username,
    String email,
    String fullName,
    LocalDateTime createdAt
) {
    
}
