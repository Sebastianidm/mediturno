package com.mediturno.mediturno.controller;

import com.mediturno.mediturno.modules.user.model.Usuario;
import com.mediturno.mediturno.modules.user.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medicos")
public class MedicoController {

    private final UsuarioRepository usuarioRepository;

    public MedicoController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarMedicos(
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(usuarioRepository.searchMedicos(especialidad, nombre));
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<String>> listarEspecialidades() {
        return ResponseEntity.ok(usuarioRepository.findAllEspecialidades());
    }
}
