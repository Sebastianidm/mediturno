package com.mediturno.mediturno.modules.appointment.service;

import com.mediturno.mediturno.exception.HorarioNoDisponibleException;
import com.mediturno.mediturno.exception.ResourceNotFoundException;
import com.mediturno.mediturno.exception.TurnoNoCancelableException;
import com.mediturno.mediturno.modules.appointment.dto.ReservaTurnoRequest;
import com.mediturno.mediturno.modules.appointment.dto.TurnoResponse;
import com.mediturno.mediturno.modules.appointment.model.AgendaMedica;
import com.mediturno.mediturno.modules.appointment.model.EstadoTurno;
import com.mediturno.mediturno.modules.appointment.model.Turno;
import com.mediturno.mediturno.modules.appointment.repository.AgendaMedicaRepository;
import com.mediturno.mediturno.modules.appointment.repository.TurnoRepository;
import com.mediturno.mediturno.modules.notification.service.EmailService;
import com.mediturno.mediturno.modules.user.model.Usuario;
import com.mediturno.mediturno.modules.user.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TurnoService {

    private static final Logger log = LoggerFactory.getLogger(TurnoService.class);

    private final TurnoRepository turnoRepository;
    private final AgendaMedicaRepository agendaMedicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public TurnoService(TurnoRepository turnoRepository,
                        AgendaMedicaRepository agendaMedicaRepository,
                        UsuarioRepository usuarioRepository,
                        EmailService emailService) {
        this.turnoRepository = turnoRepository;
        this.agendaMedicaRepository = agendaMedicaRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    @Transactional
    public TurnoResponse reservarTurno(ReservaTurnoRequest request, String emailPaciente) {
        log.info("Iniciando reserva de turno para el paciente {} en la agenda {}", emailPaciente, request.agendaId());

        Usuario paciente = usuarioRepository.findByEmail(emailPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailPaciente));

        AgendaMedica agenda = agendaMedicaRepository.findById(request.agendaId())
                .orElseThrow(() -> new ResourceNotFoundException("AgendaMedica", "id", request.agendaId()));

        if (!agenda.getDisponible()) {
            throw new HorarioNoDisponibleException("El horario ya no está disponible para reserva");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime agendaDateTime = LocalDateTime.of(agenda.getFecha(), agenda.getHoraInicio());
        if (!agendaDateTime.isAfter(now)) {
            throw new IllegalArgumentException("No se puede reservar un turno en el pasado");
        }

        boolean tieneSolapamiento = turnoRepository.existsOverlappingTurno(
                paciente.getId(), agenda.getFecha(), agenda.getHoraInicio(), agenda.getHoraFin()
        );
        if (tieneSolapamiento) {
            throw new HorarioNoDisponibleException("El paciente ya tiene un turno programado que se solapa con este horario");
        }

        Turno turno = Turno.builder()
                .paciente(paciente)
                .agenda(agenda)
                .estado(EstadoTurno.CONFIRMADO)
                .observaciones(request.observaciones())
                .build();

        agenda.setDisponible(false);
        agendaMedicaRepository.save(agenda);
        turno = turnoRepository.save(turno);

        log.info("Turno reservado exitosamente con ID {} para el paciente {}", turno.getId(), emailPaciente);

        try {
            emailService.enviarConfirmacionReserva(
                    paciente.getEmail(),
                    paciente.getNombre(),
                    agenda.getMedico().getNombre() + " " + agenda.getMedico().getApellido(),
                    agenda.getFecha().toString(),
                    agenda.getHoraInicio().toString()
            );
        } catch (Exception e) {
            log.warn("Error al enviar correo electrónico de confirmación de reserva: {}", e.getMessage());
        }

        return mapToResponse(turno);
    }

    @Transactional
    public TurnoResponse cancelarTurno(Long id, String emailUsuario) {
        log.info("Iniciando cancelación del turno {} por el usuario {}", id, emailUsuario);

        Usuario currentUser = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailUsuario));

        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno", "id", id));

        boolean esAdminOrMedico = currentUser.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equals("ROLE_ADMIN") || rol.getNombre().equals("ROLE_MEDICO"));

        // Regla 2: Verificar explícitamente la pertenencia del turno antes de cualquier otra validación
        if (!esAdminOrMedico) {
            if (!turno.getPaciente().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("No tienes permiso para cancelar este turno");
            }
        }

        // Validación de tiempo de cancelación para el paciente (mínimo 2 horas)
        if (!esAdminOrMedico) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime slotStart = LocalDateTime.of(turno.getAgenda().getFecha(), turno.getAgenda().getHoraInicio());
            if (now.plusHours(2).isAfter(slotStart)) {
                throw new TurnoNoCancelableException("La cancelación con menos de 2 horas de anticipación no está permitida para el paciente");
            }
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        turno.getAgenda().setDisponible(true);

        agendaMedicaRepository.save(turno.getAgenda());
        turno = turnoRepository.save(turno);

        log.info("Turno {} cancelado exitosamente por {}", id, emailUsuario);

        try {
            emailService.enviarConfirmacionCancelacion(
                    turno.getPaciente().getEmail(),
                    turno.getPaciente().getNombre(),
                    turno.getAgenda().getMedico().getNombre() + " " + turno.getAgenda().getMedico().getApellido(),
                    turno.getAgenda().getFecha().toString(),
                    turno.getAgenda().getHoraInicio().toString()
            );
        } catch (Exception e) {
            log.warn("Error al enviar correo electrónico de confirmación de cancelación: {}", e.getMessage());
        }

        return mapToResponse(turno);
    }

    @Transactional(readOnly = true)
    public Page<TurnoResponse> listarMisTurnos(String emailUsuario, Pageable pageable) {
        Usuario currentUser = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailUsuario));

        boolean esAdmin = currentUser.getRoles().stream().anyMatch(rol -> rol.getNombre().equals("ROLE_ADMIN"));
        boolean esMedico = currentUser.getRoles().stream().anyMatch(rol -> rol.getNombre().equals("ROLE_MEDICO"));

        Page<Turno> turnosPage;
        if (esAdmin) {
            turnosPage = turnoRepository.findAll(pageable);
        } else if (esMedico) {
            turnosPage = turnoRepository.findByAgendaMedicoId(currentUser.getId(), pageable);
        } else {
            turnosPage = turnoRepository.findByPacienteId(currentUser.getId(), pageable);
        }

        return turnosPage.map(this::mapToResponse);
    }

    private TurnoResponse mapToResponse(Turno turno) {
        return new TurnoResponse(
                turno.getId(),
                turno.getPaciente().getId(),
                turno.getPaciente().getNombre(),
                turno.getPaciente().getApellido(),
                turno.getAgenda().getId(),
                turno.getAgenda().getFecha(),
                turno.getAgenda().getHoraInicio(),
                turno.getAgenda().getHoraFin(),
                turno.getAgenda().getMedico().getNombre(),
                turno.getAgenda().getMedico().getApellido(),
                turno.getEstado().name(),
                turno.getFechaReserva(),
                turno.getObservaciones(),
                turno.getAgenda().getMedico().getEspecialidad()
        );
    }
}
