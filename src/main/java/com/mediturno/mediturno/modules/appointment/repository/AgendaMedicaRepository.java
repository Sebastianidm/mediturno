package com.mediturno.mediturno.modules.appointment.repository;

import com.mediturno.mediturno.modules.appointment.model.AgendaMedica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalTime;

public interface AgendaMedicaRepository extends JpaRepository<AgendaMedica, Long> {
    
    Page<AgendaMedica> findByMedicoIdAndDisponibleTrue(Long medicoId, Pageable pageable);
    
    Page<AgendaMedica> findByFechaAndDisponibleTrue(LocalDate fecha, Pageable pageable);

    Page<AgendaMedica> findByFechaAndMedicoIdAndDisponibleTrue(LocalDate fecha, Long medicoId, Pageable pageable);

    Page<AgendaMedica> findByDisponibleTrue(Pageable pageable);

    @Query("SELECT COUNT(a) > 0 FROM AgendaMedica a WHERE a.medico.id = :medicoId AND a.fecha = :fecha AND a.horaInicio < :horaFin AND a.horaFin > :horaInicio")
    boolean existsOverlapping(Long medicoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);
}