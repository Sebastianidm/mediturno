package com.mediturno.mediturno.modules.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mediturno.mediturno.modules.user.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_MEDICO' AND u.activo = true")
    List<Usuario> findAllMedicosActivos();

    @Query("SELECT DISTINCT u.especialidad FROM Usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_MEDICO' AND u.activo = true AND u.especialidad IS NOT NULL")
    List<String> findAllEspecialidades();

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = 'ROLE_MEDICO' AND u.activo = true " +
           "AND (:especialidad IS NULL OR :especialidad = '' OR LOWER(u.especialidad) = LOWER(:especialidad)) " +
           "AND (:nombre IS NULL OR :nombre = '' OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    List<Usuario> searchMedicos(@Param("especialidad") String especialidad, @Param("nombre") String nombre);
}
