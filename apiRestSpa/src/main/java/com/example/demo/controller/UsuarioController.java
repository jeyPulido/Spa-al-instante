package com.example.demo.controller;

import com.example.demo.dto.PerfilUpdateRequest;
import com.example.demo.entity.Usuario;
import com.example.demo.security.JwtService;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin
public class UsuarioController {

	private final UsuarioRepository usuarioRepo;
	private final JwtService jwtService;

	public UsuarioController(UsuarioRepository usuarioRepo, JwtService jwtService) {
		this.usuarioRepo = usuarioRepo;
		this.jwtService = jwtService;
	}

	@PutMapping("/perfil/{id}")
	public String actualizarPerfil(@PathVariable Long id, @RequestBody PerfilUpdateRequest req) {
		Usuario u = usuarioRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		u.setNombre(req.nombre);
		u.setApellidos(req.apellidos);
		u.setTelefono(req.telefono);

		usuarioRepo.save(u);

		return jwtService.generarToken(u);
	}
}
