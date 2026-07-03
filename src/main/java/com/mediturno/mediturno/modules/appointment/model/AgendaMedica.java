package com.mediturno.mediturno.modules.appointment.model;

import com.mediturno.mediturno.modules.user.model.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agendas_medicas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"medico_id", "fecha", "hora_inicio"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendaMedica {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "medico_id", nullable = false)
    private Usuario medico;
    
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Builder.Default
    private Boolean disponible = true;
}
