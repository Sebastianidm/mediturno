package com.mediturno.mediturno.controller;

import com.mediturno.mediturno.security.dto.AuthResponse;
import com.mediturno.mediturno.security.dto.RegistroMedicoRequest;
import com.mediturno.mediturno.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/medicos")
    public ResponseEntity<AuthResponse> registrarMedico(@Valid @RequestBody RegistroMedicoRequest request) {
        AuthResponse respuesta = adminService.registrarMedico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
