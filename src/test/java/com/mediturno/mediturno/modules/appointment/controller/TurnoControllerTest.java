package com.mediturno.mediturno.modules.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediturno.mediturno.modules.appointment.dto.ReservaTurnoRequest;
import com.mediturno.mediturno.modules.appointment.dto.TurnoResponse;
import com.mediturno.mediturno.modules.appointment.service.TurnoService;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TurnoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private TurnoService turnoService;

    @InjectMocks
    private TurnoController turnoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(turnoController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new HandlerMethodArgumentResolver() {
                            @Override
                            public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                                return parameter.getParameterType().equals(UserDetails.class);
                            }

                            @Override
                            public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                                          ModelAndViewContainer mavContainer,
                                                          NativeWebRequest webRequest,
                                                          WebDataBinderFactory binderFactory) {
                                return User.withUsername("paciente@email.com")
                                        .password("password")
                                        .authorities("ROLE_PACIENTE")
                                        .build();
                            }
                        }
                )
                .build();
    }

    @Test
    void reservarTurno_Success() throws Exception {
        ReservaTurnoRequest request = new ReservaTurnoRequest(1L, "Consulta general");
        TurnoResponse response = new TurnoResponse(1L, 2L, "Pedro", "Gómez", 1L, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "Marta", "Sánchez", "CONFIRMADO", ZonedDateTime.now(), "Consulta general");

        when(turnoService.reservarTurno(any(ReservaTurnoRequest.class), eq("paciente@email.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void cancelarTurno_Success() throws Exception {
        TurnoResponse response = new TurnoResponse(1L, 2L, "Pedro", "Gómez", 1L, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "Marta", "Sánchez", "CANCELADO", ZonedDateTime.now(), "Consulta general");
        when(turnoService.cancelarTurno(eq(1L), eq("paciente@email.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/turnos/1/cancelar"))
                .andExpect(status().isOk());
    }

    @Test
    void listarMisTurnos_Success() throws Exception {
        when(turnoService.listarMisTurnos(eq("paciente@email.com"), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/v1/turnos/mis-turnos"))
                .andExpect(status().isOk());
    }
}
