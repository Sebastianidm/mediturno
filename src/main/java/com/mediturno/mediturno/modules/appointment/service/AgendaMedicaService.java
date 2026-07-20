package com.mediturno.mediturno.modules.appointment.service;

import com.mediturno.mediturno.exception.HorarioNoDisponibleException;
import com.mediturno.mediturno.exception.ResourceNotFoundException;
import com.mediturno.mediturno.modules.appointment.dto.AgendaRequest;
import com.mediturno.mediturno.modules.appointment.dto.AgendaResponse;
import com.mediturno.mediturno.modules.appointment.model.AgendaMedica;
import com.mediturno.mediturno.modules.appointment.repository.AgendaMedicaRepository;
import com.mediturno.mediturno.modules.user.model.Usuario;
import com.mediturno.mediturno.modules.user.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AgendaMedicaService {

    private final AgendaMedicaRepository agendaMedicaRepository;
    private final UsuarioRepository usuarioRepository;

    public AgendaMedicaService(AgendaMedicaRepository agendaMedicaRepository, UsuarioRepository usuarioRepository) {
        this.agendaMedicaRepository = agendaMedicaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AgendaResponse registrarAgenda(AgendaRequest request) {
        Usuario medico = usuarioRepository.findById(request.medicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.medicoId()));

        boolean esMedico = medico.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equals("ROLE_MEDICO"));

        if (!esMedico) {
            throw new IllegalArgumentException("El usuario especificado no tiene el rol de médico");
        }

        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        boolean existeSolapamiento = agendaMedicaRepository.existsOverlapping(
                request.medicoId(), request.fecha(), request.horaInicio(), request.horaFin()
        );

        if (existeSolapamiento) {
            throw new HorarioNoDisponibleException("El médico ya cuenta con disponibilidad registrada en ese horario");
        }

        AgendaMedica agenda = AgendaMedica.builder()
                .medico(medico)
                .fecha(request.fecha())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .disponible(true)
                .build();

        agenda = agendaMedicaRepository.save(agenda);

        return mapToResponse(agenda);
    }

    @Transactional(readOnly = true)
    public Page<AgendaResponse> obtenerSlotsLibres(LocalDate fecha, Long medicoId, String especialidad, Pageable pageable) {
        Page<AgendaMedica> page = agendaMedicaRepository.findSlotsLibres(fecha, medicoId, especialidad, pageable);
        return page.map(this::mapToResponse);
    }

    private AgendaResponse mapToResponse(AgendaMedica agenda) {
        return new AgendaResponse(
                agenda.getId(),
                agenda.getMedico().getId(),
                agenda.getMedico().getNombre(),
                agenda.getMedico().getApellido(),
                agenda.getFecha(),
                agenda.getHoraInicio(),
                agenda.getHoraFin(),
                agenda.getDisponible(),
                agenda.getMedico().getEspecialidad()
        );
    }
}
