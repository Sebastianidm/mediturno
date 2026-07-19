package com.mediturno.mediturno.modules.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public record TurnoResponse(
    Long id,
    Long pacienteId,
    String pacienteNombre,
    String pacienteApellido,
    Long agendaId,
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin,
    String medicoNombre,
    String medicoApellido,
    String estado,
    ZonedDateTime fechaReserva,
    String observaciones
) {}
