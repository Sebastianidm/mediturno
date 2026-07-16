package com.mediturno.mediturno.security.service;



import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mediturno.mediturno.modules.user.model.Usuario;
import com.mediturno.mediturno.modules.user.repository.UsuarioRepository;

import com.mediturno.mediturno.security.dto.AuthResponse;
import com.mediturno.mediturno.security.dto.LoginRequest;
import com.mediturno.mediturno.security.dto.RegistroRequest;
import com.mediturno.mediturno.security.jwt.JwtService;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;



    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UsuarioRepository usuarioRepository,  PasswordEncoder passwordEncoder){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse registrar(RegistroRequest request){

      if( usuarioRepository.existsByEmail(request.email())) {
        throw new RuntimeException("El correo ya está registrado");
      }
      Usuario nuevoUsuario = new Usuario();
      nuevoUsuario.setEmail(request.email());
      nuevoUsuario.setNombre(request.nombre());
      nuevoUsuario.setApellido(request.apellido());

      String passwordEncriptada = passwordEncoder.encode(request.password());
      nuevoUsuario.setPassword(passwordEncriptada);

      usuarioRepository.save(nuevoUsuario);

      String token = jwtService.generateToken(nuevoUsuario.getEmail());

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

      String token = jwtService.generateToken(usuario.getEmail());

      return new AuthResponse(
        token,
        "Bearer",
         usuario.getEmail(),
        usuario.getNombre(),
        usuario.getApellido()
       
      );

    }
    
}
