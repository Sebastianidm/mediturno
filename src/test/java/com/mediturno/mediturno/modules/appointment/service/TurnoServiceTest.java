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
import com.mediturno.mediturno.modules.user.model.Rol;
import com.mediturno.mediturno.modules.user.model.Usuario;
import com.mediturno.mediturno.modules.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private AgendaMedicaRepository agendaMedicaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private TurnoService turnoService;

    private Usuario paciente;
    private Usuario medico;
    private Usuario admin;
    private AgendaMedica agenda;
    private Turno turno;

    @BeforeEach
    void setUp() {
        paciente = Usuario.builder()
                .id(2L)
                .nombre("Pedro")
                .apellido("Gómez")
                .email("pedro.gomez@paciente.com")
                .roles(Set.of(new Rol(1, "ROLE_PACIENTE")))
                .activo(true)
                .build();

        medico = Usuario.builder()
                .id(3L)
                .nombre("Marta")
                .apellido("Sánchez")
                .email("marta.sanchez@medico.com")
                .roles(Set.of(new Rol(2, "ROLE_MEDICO")))
                .activo(true)
                .build();

        admin = Usuario.builder()
                .id(4L)
                .nombre("Admin")
                .apellido("System")
                .email("admin@mediturno.com")
                .roles(Set.of(new Rol(3, "ROLE_ADMIN")))
                .activo(true)
                .build();

        agenda = AgendaMedica.builder()
                .id(100L)
                .medico(medico)
                .fecha(LocalDate.now().plusDays(2))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(11, 0))
                .disponible(true)
                .build();

        turno = Turno.builder()
                .id(200L)
                .paciente(paciente)
                .agenda(agenda)
                .estado(EstadoTurno.CONFIRMADO)
                .fechaReserva(ZonedDateTime.now())
                .observaciones("Primera consulta")
                .build();
    }

    @Test
    void reservarTurno_exito() {
        ReservaTurnoRequest request = new ReservaTurnoRequest(100L, "Primera consulta");
        when(usuarioRepository.findByEmail("pedro.gomez@paciente.com")).thenReturn(Optional.of(paciente));
        when(agendaMedicaRepository.findById(100L)).thenReturn(Optional.of(agenda));
        when(turnoRepository.existsOverlappingTurno(eq(2L), any(), any(), any())).thenReturn(false);
        when(turnoRepository.save(any(Turno.class))).thenReturn(turno);

        TurnoResponse response = turnoService.reservarTurno(request, "pedro.gomez@paciente.com");

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertFalse(agenda.getDisponible());
        verify(emailService, times(1)).enviarConfirmacionReserva(any(), any(), any(), any(), any());
    }

    @Test
    void reservarTurno_noDisponible() {
        agenda.setDisponible(false);
        ReservaTurnoRequest request = new ReservaTurnoRequest(100L, "Primera consulta");
        when(usuarioRepository.findByEmail("pedro.gomez@paciente.com")).thenReturn(Optional.of(paciente));
        when(agendaMedicaRepository.findById(100L)).thenReturn(Optional.of(agenda));

        assertThrows(HorarioNoDisponibleException.class, () -> turnoService.reservarTurno(request, "pedro.gomez@paciente.com"));
    }

    @Test
    void reservarTurno_enElPasado() {
        agenda.setFecha(LocalDate.now().minusDays(1));
        ReservaTurnoRequest request = new ReservaTurnoRequest(100L, "Primera consulta");
        when(usuarioRepository.findByEmail("pedro.gomez@paciente.com")).thenReturn(Optional.of(paciente));
        when(agendaMedicaRepository.findById(100L)).thenReturn(Optional.of(agenda));

        assertThrows(IllegalArgumentException.class, () -> turnoService.reservarTurno(request, "pedro.gomez@paciente.com"));
    }

    @Test
    void reservarTurno_solapamiento() {
        ReservaTurnoRequest request = new ReservaTurnoRequest(100L, "Primera consulta");
        when(usuarioRepository.findByEmail("pedro.gomez@paciente.com")).thenReturn(Optional.of(paciente));
        when(agendaMedicaRepository.findById(100L)).thenReturn(Optional.of(agenda));
        when(turnoRepository.existsOverlappingTurno(eq(2L), any(), any(), any())).thenReturn(true);

        assertThrows(HorarioNoDisponibleException.class, () -> turnoService.reservarTurno(request, "pedro.gomez@paciente.com"));
    }

    @Test
    void cancelarTurno_pacienteExito() {
        when(usuarioRepository.findByEmail("pedro.gomez@paciente.com")).thenReturn(Optional.of(paciente));
        when(turnoRepository.findById(200L)).thenReturn(Optional.of(turno));
        when(turnoRepository.save(any(Turno.class))).thenReturn(turno);

        TurnoResponse response = turnoService.cancelarTurno(200L, "pedro.gomez@paciente.com");

        assertNotNull(response);
        assertEquals(EstadoTurno.CANCELADO.name(), response.estado());
        assertTrue(agenda.getDisponible());
        verify(emailService, times(1)).enviarConfirmacionCancelacion(any(), any(), any(), any(), any());
    }

    @Test
    void cancelarTurno_adminExito_dentroDeLasDosHoras() {
        agenda.setFecha(LocalDate.now());
        agenda.setHoraInicio(LocalTime.now().plusMinutes(30));
        
        when(usuarioRepository.findByEmail("admin@mediturno.com")).thenReturn(Optional.of(admin));
        when(turnoRepository.findById(200L)).thenReturn(Optional.of(turno));
        when(turnoRepository.save(any(Turno.class))).thenReturn(turno);

        TurnoResponse response = turnoService.cancelarTurno(200L, "admin@mediturno.com");

        assertNotNull(response);
        assertEquals(EstadoTurno.CANCELADO.name(), response.estado());
    }

    @Test
    void cancelarTurno_pacienteTardio_error() {
        agenda.setFecha(LocalDate.now());
        agenda.setHoraInicio(LocalTime.now().plusMinutes(30));

        when(usuarioRepository.findByEmail("pedro.gomez@paciente.com")).thenReturn(Optional.of(paciente));
        when(turnoRepository.findById(200L)).thenReturn(Optional.of(turno));

        assertThrows(TurnoNoCancelableException.class, () -> turnoService.cancelarTurno(200L, "pedro.gomez@paciente.com"));
    }

    @Test
    void cancelarTurno_noDueno_error() {
        Usuario otroPaciente = Usuario.builder()
                .id(999L)
                .email("otro@paciente.com")
                .roles(Set.of(new Rol(1, "ROLE_PACIENTE")))
                .build();
        
        when(usuarioRepository.findByEmail("otro@paciente.com")).thenReturn(Optional.of(otroPaciente));
        when(turnoRepository.findById(200L)).thenReturn(Optional.of(turno));

        assertThrows(AccessDeniedException.class, () -> turnoService.cancelarTurno(200L, "otro@paciente.com"));
    }

    @Test
    void listarMisTurnos_paciente() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Turno> page = new PageImpl<>(Collections.singletonList(turno));
        when(usuarioRepository.findByEmail("pedro.gomez@paciente.com")).thenReturn(Optional.of(paciente));
        when(turnoRepository.findByPacienteId(2L, pageable)).thenReturn(page);

        Page<TurnoResponse> responsePage = turnoService.listarMisTurnos("pedro.gomez@paciente.com", pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
    }

    @Test
    void listarMisTurnos_medico() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Turno> page = new PageImpl<>(Collections.singletonList(turno));
        when(usuarioRepository.findByEmail("marta.sanchez@medico.com")).thenReturn(Optional.of(medico));
        when(turnoRepository.findByAgendaMedicoId(3L, pageable)).thenReturn(page);

        Page<TurnoResponse> responsePage = turnoService.listarMisTurnos("marta.sanchez@medico.com", pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
    }

    @Test
    void listarMisTurnos_admin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Turno> page = new PageImpl<>(Collections.singletonList(turno));
        when(usuarioRepository.findByEmail("admin@mediturno.com")).thenReturn(Optional.of(admin));
        when(turnoRepository.findAll(pageable)).thenReturn(page);

        Page<TurnoResponse> responsePage = turnoService.listarMisTurnos("admin@mediturno.com", pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
    }
}
