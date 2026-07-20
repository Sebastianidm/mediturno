package com.mediturno.mediturno.modules.appointment.service;

import com.mediturno.mediturno.exception.HorarioNoDisponibleException;
import com.mediturno.mediturno.exception.ResourceNotFoundException;
import com.mediturno.mediturno.modules.appointment.dto.AgendaRequest;
import com.mediturno.mediturno.modules.appointment.dto.AgendaResponse;
import com.mediturno.mediturno.modules.appointment.model.AgendaMedica;
import com.mediturno.mediturno.modules.appointment.repository.AgendaMedicaRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaMedicaServiceTest {

    @Mock
    private AgendaMedicaRepository agendaMedicaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AgendaMedicaService agendaMedicaService;

    private Usuario medico;
    private Rol rolMedico;

    @BeforeEach
    void setUp() {
        rolMedico = new Rol(2, "ROLE_MEDICO");
        medico = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@mediturno.com")
                .password("encoded_pass")
                .roles(Set.of(rolMedico))
                .activo(true)
                .build();
    }

    @Test
    void registrarAgenda_exito() {
        AgendaRequest request = new AgendaRequest(1L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(agendaMedicaRepository.existsOverlapping(eq(1L), any(), any(), any())).thenReturn(false);
        
        AgendaMedica savedAgenda = AgendaMedica.builder()
                .id(10L)
                .medico(medico)
                .fecha(request.fecha())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .disponible(true)
                .build();
        when(agendaMedicaRepository.save(any(AgendaMedica.class))).thenReturn(savedAgenda);

        AgendaResponse response = agendaMedicaService.registrarAgenda(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Juan", response.medicoNombre());
        assertTrue(response.disponible());
    }

    @Test
    void registrarAgenda_usuarioNoExiste() {
        AgendaRequest request = new AgendaRequest(99L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> agendaMedicaService.registrarAgenda(request));
    }

    @Test
    void registrarAgenda_usuarioNoEsMedico() {
        Usuario paciente = Usuario.builder()
                .id(2L)
                .roles(Set.of(new Rol(1, "ROLE_PACIENTE")))
                .build();
        AgendaRequest request = new AgendaRequest(2L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(paciente));

        assertThrows(IllegalArgumentException.class, () -> agendaMedicaService.registrarAgenda(request));
    }

    @Test
    void registrarAgenda_horaFinAntesQueInicio() {
        AgendaRequest request = new AgendaRequest(1L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(9, 0));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(medico));

        assertThrows(IllegalArgumentException.class, () -> agendaMedicaService.registrarAgenda(request));
    }

    @Test
    void registrarAgenda_solapamiento() {
        AgendaRequest request = new AgendaRequest(1L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(agendaMedicaRepository.existsOverlapping(eq(1L), any(), any(), any())).thenReturn(true);

        assertThrows(HorarioNoDisponibleException.class, () -> agendaMedicaService.registrarAgenda(request));
    }

    @Test
    void obtenerSlotsLibres_filtroCompleto() {
        LocalDate fecha = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        
        AgendaMedica agenda = AgendaMedica.builder().id(10L).medico(medico).fecha(fecha).horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(10, 0)).disponible(true).build();
        Page<AgendaMedica> page = new PageImpl<>(Collections.singletonList(agenda));
        
        when(agendaMedicaRepository.findSlotsLibres(fecha, 1L, null, pageable)).thenReturn(page);

        Page<AgendaResponse> responsePage = agendaMedicaService.obtenerSlotsLibres(fecha, 1L, null, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
        assertEquals(10L, responsePage.getContent().get(0).id());
    }

    @Test
    void obtenerSlotsLibres_filtroFecha() {
        LocalDate fecha = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        
        AgendaMedica agenda = AgendaMedica.builder().id(10L).medico(medico).fecha(fecha).horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(10, 0)).disponible(true).build();
        Page<AgendaMedica> page = new PageImpl<>(Collections.singletonList(agenda));
        
        when(agendaMedicaRepository.findSlotsLibres(fecha, null, null, pageable)).thenReturn(page);

        Page<AgendaResponse> responsePage = agendaMedicaService.obtenerSlotsLibres(fecha, null, null, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
    }

    @Test
    void obtenerSlotsLibres_filtroMedico() {
        Pageable pageable = PageRequest.of(0, 10);
        
        AgendaMedica agenda = AgendaMedica.builder().id(10L).medico(medico).fecha(LocalDate.now()).horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(10, 0)).disponible(true).build();
        Page<AgendaMedica> page = new PageImpl<>(Collections.singletonList(agenda));
        
        when(agendaMedicaRepository.findSlotsLibres(null, 1L, null, pageable)).thenReturn(page);

        Page<AgendaResponse> responsePage = agendaMedicaService.obtenerSlotsLibres(null, 1L, null, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
    }

    @Test
    void obtenerSlotsLibres_sinFiltro() {
        Pageable pageable = PageRequest.of(0, 10);
        
        AgendaMedica agenda = AgendaMedica.builder().id(10L).medico(medico).fecha(LocalDate.now()).horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(10, 0)).disponible(true).build();
        Page<AgendaMedica> page = new PageImpl<>(Collections.singletonList(agenda));
        
        when(agendaMedicaRepository.findSlotsLibres(null, null, null, pageable)).thenReturn(page);

        Page<AgendaResponse> responsePage = agendaMedicaService.obtenerSlotsLibres(null, null, null, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
    }
}
