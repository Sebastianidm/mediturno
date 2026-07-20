package com.mediturno.mediturno.modules.appointment.controller;

import com.mediturno.mediturno.exception.ErrorResponse;
import com.mediturno.mediturno.modules.appointment.dto.AgendaRequest;
import com.mediturno.mediturno.modules.appointment.dto.AgendaResponse;
import com.mediturno.mediturno.modules.appointment.service.AgendaMedicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaMedicaController {

    private final AgendaMedicaService agendaMedicaService;

    public AgendaMedicaController(AgendaMedicaService agendaMedicaService) {
        this.agendaMedicaService = agendaMedicaService;
    }

    @Operation(summary = "Crear disponibilidad médica", description = "Permite a un administrador o a un médico registrar un bloque horario de disponibilidad.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Disponibilidad registrada exitosamente",
                     content = @Content(schema = @Schema(implementation = AgendaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o error de validación",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido - Se requiere rol ADMIN o MEDICO",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto - El horario ya está solapado con otra disponibilidad del médico",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    public ResponseEntity<AgendaResponse> crearAgenda(@Valid @RequestBody AgendaRequest request) {
        AgendaResponse respuesta = agendaMedicaService.registrarAgenda(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @Operation(summary = "Consultar slots disponibles", description = "Permite al público y pacientes consultar los bloques horarios de disponibilidad libres en el sistema con paginación.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado de slots disponibles obtenido exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<AgendaResponse>> listarSlotsLibres(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) String especialidad,
            Pageable pageable) {
        Page<AgendaResponse> respuesta = agendaMedicaService.obtenerSlotsLibres(fecha, medicoId, especialidad, pageable);
        return ResponseEntity.ok(respuesta);
    }
}
