package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegistroRequest;
import com.example.demo.entity.Rol;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.jwtService = jwtService;
	}

	public String login(LoginRequest request) {

		Usuario usuario = usuarioRepository.findByCorreo(request.correo)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		if (!encoder.matches(request.password, usuario.getPassword())) {
			throw new RuntimeException("Credenciales incorrectas");
		}

		return jwtService.generarToken(usuario);
	}

	public void register(RegistroRequest request) {

		if (usuarioRepository.existsByCorreo(request.correo)) {
			throw new RuntimeException("Correo ya registrado");
		}

		Usuario u = new Usuario();
		u.setNombre(request.nombre);
		u.setApellidos(request.apellidos);
		u.setCorreo(request.correo);
		u.setTelefono(request.telefono);
		u.setPassword(encoder.encode(request.password));

		u.setRol(request.rol != null ? request.rol : Rol.CLIENTE);

		usuarioRepository.save(u);
	}

}
