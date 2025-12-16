package com.example.demo.controller;

import com.example.demo.dto.CitaRequest;
import com.example.demo.dto.HorarioDTO;
import com.example.demo.entity.Cita;
import com.example.demo.entity.Servicio;
import com.example.demo.repository.ServicioRepository;
import com.example.demo.service.CitaService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class CitaController {

    private final CitaService citaService;
    private final ServicioRepository servicioRepository;

    public CitaController(CitaService citaService, ServicioRepository servicioRepository) {
        this.citaService = citaService;
        this.servicioRepository = servicioRepository;
    }

    @GetMapping("/servicios")
    public List<Servicio> obtenerServicios() {
        return servicioRepository.findAll();
    }

    // ✅ HORARIOS CON DISPONIBILIDAD
    @GetMapping("/horarios-disponibles")
    public List<HorarioDTO> obtenerHorarios(@RequestParam String fecha) {
        return citaService.obtenerHorariosDisponibles(LocalDate.parse(fecha));
    }

    // ✅ DÍAS SATURADOS
    @GetMapping("/dias-saturados")
    public List<String> obtenerDiasSaturados() {
        return citaService.obtenerDiasSaturados()
                .stream()
                .map(LocalDate::toString)
                .toList();
    }

    @GetMapping("/citas")
    public List<Cita> obtenerCitas() {
        return citaService.obtenerTodas();
    }

    @PostMapping("/citas")
    public Cita crearCita(@RequestBody CitaRequest request) {
        return citaService.crearCita(request);
    }

    @DeleteMapping("/citas/{id}")
    public void cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
    }
}
