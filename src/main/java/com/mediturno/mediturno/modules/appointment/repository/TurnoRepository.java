package com.mediturno.mediturno.modules.appointment.repository;

import com.mediturno.mediturno.modules.appointment.model.Turno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalTime;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    Page<Turno> findByPacienteId(Long pacienteId, Pageable pageable);
    
    Page<Turno> findByAgendaMedicoId(Long medicoId, Pageable pageable);

    @Query("SELECT COUNT(t) > 0 FROM Turno t WHERE t.paciente.id = :pacienteId AND t.estado = 'CONFIRMADO' AND t.agenda.fecha = :fecha AND t.agenda.horaInicio < :horaFin AND t.agenda.horaFin > :horaInicio")
    boolean existsOverlappingTurno(Long pacienteId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);
}