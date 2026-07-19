package com.mediturno.mediturno.modules.appointment.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record AgendaRequest(
    @NotNull(message = "El ID del médico es obligatorio") Long medicoId,
    @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
    @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,
    @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin
) {
    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    public boolean isHoraFinDespuesDeHoraInicio() {
        if (horaInicio == null || horaFin == null) {
            return true;
        }
        return horaFin.isAfter(horaInicio);
    }

    @AssertTrue(message = "La fecha y hora de inicio de la agenda debe ser en el futuro")
    public boolean isFechaHoraFutura() {
        if (fecha == null || horaInicio == null) {
            return true;
        }
        return LocalDateTime.of(fecha, horaInicio).isAfter(LocalDateTime.now());
    }
}
