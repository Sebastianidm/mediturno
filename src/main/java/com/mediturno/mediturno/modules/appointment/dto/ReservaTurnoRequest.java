package com.mediturno.mediturno.modules.appointment.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaTurnoRequest(
    @NotNull(message = "El ID de la agenda es obligatorio") Long agendaId,
    String observaciones
) {}
