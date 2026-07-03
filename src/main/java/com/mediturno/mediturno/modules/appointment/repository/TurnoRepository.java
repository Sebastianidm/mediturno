package com.mediturno.mediturno.modules.appointment.repository;

import com.mediturno.mediturno.modules.appointment.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    // Historial médico: Permite al paciente ver todos sus turnos
    List<Turno> findByPacienteId(Long pacienteId);
    
    // Agenda del día: Permite al médico ver qué pacientes tiene agendados a través de su disponibilidad
    List<Turno> findByAgendaMedicoId(Long medicoId);
}