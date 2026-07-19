package com.mediturno.mediturno.modules.appointment.controller;

import com.mediturno.mediturno.exception.ErrorResponse;
import com.mediturno.mediturno.modules.appointment.dto.ReservaTurnoRequest;
import com.mediturno.mediturno.modules.appointment.dto.TurnoResponse;
import com.mediturno.mediturno.modules.appointment.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @Operation(summary = "Reservar un nuevo turno", description = "Permite a un usuario con rol PACIENTE reservar una cita médica en un bloque horario disponible.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Turno reservado exitosamente",
                     content = @Content(schema = @Schema(implementation = TurnoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o turno en el pasado",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido - Se requiere rol PACIENTE",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto - El horario ya no está disponible o el paciente tiene solapamientos",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<TurnoResponse> reservarTurno(
            @Valid @RequestBody ReservaTurnoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TurnoResponse respuesta = turnoService.reservarTurno(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @Operation(summary = "Cancelar un turno", description = "Permite a un paciente cancelar su propio turno (con límite de 2h de anticipación), o a médicos/admins cancelarlo libremente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Turno cancelado exitosamente",
                     content = @Content(schema = @Schema(implementation = TurnoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Cancelación tardía (menos de 2 horas de anticipación)",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido - No eres dueño del turno ni tienes rol médico/admin",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<TurnoResponse> cancelarTurno(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        TurnoResponse respuesta = turnoService.cancelarTurno(id, userDetails.getUsername());
        return ResponseEntity.ok(respuesta);
    }

    @Operation(summary = "Listar mis turnos", description = "Devuelve el listado de turnos de la sesión actual de forma paginada: pacientes ven sus reservas, médicos su agenda asignada, admins todos los turnos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado de turnos obtenido exitosamente")
    })
    @GetMapping("/mis-turnos")
    public ResponseEntity<Page<TurnoResponse>> listarMisTurnos(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<TurnoResponse> respuesta = turnoService.listarMisTurnos(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(respuesta);
    }
}
