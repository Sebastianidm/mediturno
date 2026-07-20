package com.mediturno.mediturno.service;

import com.mediturno.mediturno.modules.user.model.Rol;
import com.mediturno.mediturno.modules.user.model.Usuario;
import com.mediturno.mediturno.modules.user.repository.RolRepository;
import com.mediturno.mediturno.modules.user.repository.UsuarioRepository;
import com.mediturno.mediturno.security.dto.AuthResponse;
import com.mediturno.mediturno.security.dto.RegistroMedicoRequest;
import com.mediturno.mediturno.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mediturno.mediturno.exception.UserAlreadyExistsException;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrarMedico(RegistroMedicoRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("El correo ya está registrado");
        }

        Rol rolMedico = rolRepository.findByNombre("ROLE_MEDICO")
                .orElseThrow(() -> new RuntimeException("Rol ROLE_MEDICO no encontrado"));

        Usuario medico = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .especialidad(request.especialidad())
                .build();

        medico.getRoles().add(rolMedico);
        usuarioRepository.save(medico);

        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("id", medico.getId());
        claims.put("nombre", medico.getNombre() + " " + medico.getApellido());
        claims.put("email", medico.getEmail());

        String rol = medico.getRoles().stream()
                .map(Rol::getNombre)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .findFirst()
                .orElse("MEDICO");
        claims.put("rol", rol);

        String token = jwtService.generateToken(medico.getEmail(), claims);

        return new AuthResponse(
                token,
                "Bearer",
                medico.getEmail(),
                medico.getNombre(),
                medico.getApellido()
        );
    }
}
