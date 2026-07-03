package com.mediturno.mediturno.modules.user.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name ="roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    
}
