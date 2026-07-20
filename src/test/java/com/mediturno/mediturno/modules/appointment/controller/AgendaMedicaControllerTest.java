package com.mediturno.mediturno.modules.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediturno.mediturno.modules.appointment.dto.AgendaRequest;
import com.mediturno.mediturno.modules.appointment.dto.AgendaResponse;
import com.mediturno.mediturno.modules.appointment.service.AgendaMedicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AgendaMedicaControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private AgendaMedicaService agendaMedicaService;

    @InjectMocks
    private AgendaMedicaController agendaMedicaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(agendaMedicaController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void crearAgenda_Success() throws Exception {
        AgendaRequest request = new AgendaRequest(1L, LocalDate.now().plusDays(2), LocalTime.of(9, 0), LocalTime.of(10, 0));
        AgendaResponse response = new AgendaResponse(10L, 1L, "Juan", "Pérez", request.fecha(), request.horaInicio(), request.horaFin(), true, "Pediatría");
        
        when(agendaMedicaService.registrarAgenda(any(AgendaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void listarSlotsLibres_Success() throws Exception {
        when(agendaMedicaService.obtenerSlotsLibres(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/v1/agendas"))
                .andExpect(status().isOk());
    }
}
