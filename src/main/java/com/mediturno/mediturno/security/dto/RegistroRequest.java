package com.mediturno.mediturno.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistroRequest(@NotBlank String nombre,@NotBlank  String apellido,@NotBlank @Email String email,@NotBlank  String password) {
   
}
