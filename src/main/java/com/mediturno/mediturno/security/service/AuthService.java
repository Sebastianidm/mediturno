package com.mediturno.mediturno.security.service;



import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mediturno.mediturno.exception.UserAlreadyExistsException;

import com.mediturno.mediturno.modules.user.model.Rol;
import com.mediturno.mediturno.modules.user.model.Usuario;
import com.mediturno.mediturno.modules.user.repository.RolRepository;
import com.mediturno.mediturno.modules.user.repository.UsuarioRepository;

import com.mediturno.mediturno.security.dto.AuthResponse;
import com.mediturno.mediturno.security.dto.LoginRequest;
import com.mediturno.mediturno.security.dto.RegistroRequest;
import com.mediturno.mediturno.security.jwt.JwtService;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request){

      if( usuarioRepository.existsByEmail(request.email())) {
        throw new UserAlreadyExistsException("El correo ya está registrado");
      }

      Rol rolPaciente = rolRepository.findByNombre("ROLE_PACIENTE")
              .orElseThrow(() -> new RuntimeException("Rol ROLE_PACIENTE no encontrado"));

      Usuario nuevoUsuario = new Usuario();
      nuevoUsuario.setEmail(request.email());
      nuevoUsuario.setNombre(request.nombre());
      nuevoUsuario.setApellido(request.apellido());
      nuevoUsuario.getRoles().add(rolPaciente);

      String passwordEncriptada = passwordEncoder.encode(request.password());
      nuevoUsuario.setPassword(passwordEncriptada);

      usuarioRepository.save(nuevoUsuario);

      String token = generateTokenWithClaims(nuevoUsuario);

      return new AuthResponse(
        token,
        "Bearer",
        nuevoUsuario.getEmail(),
        nuevoUsuario.getNombre(),
        nuevoUsuario.getApellido()
      );

    }
 
    public AuthResponse login(LoginRequest request){
      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());

      Authentication authentication = authenticationManager.authenticate(authToken);

      Usuario usuario = usuarioRepository.findByEmail(request.email())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

      String token = generateTokenWithClaims(usuario);

      return new AuthResponse(
        token,
        "Bearer",
         usuario.getEmail(),
        usuario.getNombre(),
        usuario.getApellido()
       
      );

    }

    private String generateTokenWithClaims(Usuario usuario) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("id", usuario.getId());
        claims.put("nombre", usuario.getNombre() + " " + usuario.getApellido());
        claims.put("email", usuario.getEmail());

        String rol = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .findFirst()
                .orElse("PACIENTE");
        claims.put("rol", rol);

        return jwtService.generateToken(usuario.getEmail(), claims);
    }
    
    
}
