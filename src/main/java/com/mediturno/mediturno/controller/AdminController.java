package com.mediturno.mediturno.controller;

import com.mediturno.mediturno.modules.user.model.Usuario;
import com.mediturno.mediturno.modules.user.repository.UsuarioRepository;
import com.mediturno.mediturno.security.dto.AuthResponse;
import com.mediturno.mediturno.security.dto.RegistroMedicoRequest;
import com.mediturno.mediturno.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.mediturno.mediturno.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UsuarioRepository usuarioRepository;

    public AdminController(AdminService adminService, UsuarioRepository usuarioRepository) {
        this.adminService = adminService;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Registrar un nuevo médico", description = "Permite a un usuario con rol ADMIN registrar a un nuevo médico en el sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Médico registrado exitosamente",
                     content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o error de validación",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido - Se requiere rol ADMIN",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto - El correo electrónico ya está registrado",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/medicos")
    public ResponseEntity<AuthResponse> registrarMedico(@Valid @RequestBody RegistroMedicoRequest request) {
        AuthResponse respuesta = adminService.registrarMedico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/medicos")
    @Operation(summary = "Listar médicos del sistema", description = "Permite a un administrador listar todos los médicos activos.")
    public ResponseEntity<List<Usuario>> listarMedicosParaAdmin() {
        return ResponseEntity.ok(usuarioRepository.findAllMedicosActivos());
    }
}
