package com.mediturno.mediturno.security.dto;

public record AuthResponse(String token, String tokenType, String email, String nombre, String apellido) {
    
}
