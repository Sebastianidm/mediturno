package com.mediturno.mediturno.modules.user.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mediturno.mediturno.modules.user.model.Rol;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);
    
}
