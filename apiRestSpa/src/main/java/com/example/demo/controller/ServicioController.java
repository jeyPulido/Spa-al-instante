package com.example.demo.controller;

import com.example.demo.entity.Servicio;
import com.example.demo.repository.ServicioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin("*")
public class ServicioController {

	private final ServicioRepository servicioRepository;

	public ServicioController(ServicioRepository servicioRepository) {
		this.servicioRepository = servicioRepository;
	}

	@GetMapping
	public List<Servicio> listar() {
		return servicioRepository.findAll();
	}

	@PostMapping
	public Servicio crear(@RequestBody Servicio servicio) {
		return servicioRepository.save(servicio);
	}

	@PutMapping("/{id}")
	public Servicio actualizar(@PathVariable Long id, @RequestBody Servicio servicio) {
		Servicio existente = servicioRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

		existente.setNombre(servicio.getNombre());
		existente.setDescripcion(servicio.getDescripcion());
		existente.setPrecio(servicio.getPrecio());

		return servicioRepository.save(existente);
	}

	@DeleteMapping("/{id}")
	public void eliminar(@PathVariable Long id) {
		servicioRepository.deleteById(id);
	}
}
