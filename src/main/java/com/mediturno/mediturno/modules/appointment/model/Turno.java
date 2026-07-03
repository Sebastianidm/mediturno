package com.mediturno.mediturno.modules.appointment.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import com.mediturno.mediturno.modules.user.model.Usuario;

@Entity
@Table(name = "turnos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id", nullable = false, unique = true)
    private AgendaMedica agenda;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoTurno estado = EstadoTurno.PENDIENTE;

    @Column(name = "fecha_reserva" , updatable = false)
    @Builder.Default
    private ZonedDateTime fechaReserva = ZonedDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String observaciones;


    
}
