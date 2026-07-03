package com.mediturno.mediturno.modules.appointment.repository;

import com.mediturno.mediturno.modules.appointment.model.AgendaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AgendaMedicaRepository extends JpaRepository<AgendaMedica, Long> {
    
    // Para listar los bloques disponibles de un médico específico
    List<AgendaMedica> findByMedicoIdAndDisponibleTrue(Long medicoId);
    
    // Para buscar disponibilidad en una fecha en particular
    List<AgendaMedica> findByFechaAndDisponibleTrue(LocalDate fecha);
}