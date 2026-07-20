package com.mediturno.mediturno.modules.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendaResponse(
    Long id,
    Long medicoId,
    String medicoNombre,
    String medicoApellido,
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin,
    Boolean disponible,
    String especialidad
) {}
