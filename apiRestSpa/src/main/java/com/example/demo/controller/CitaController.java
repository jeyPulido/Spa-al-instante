package com.example.demo.controller;

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

    @GetMapping("/horarios-disponibles")
    public List<String> obtenerHorarios(@RequestParam String fecha) {
        LocalDate f = LocalDate.parse(fecha);

        return citaService.obtenerHorariosDisponibles(f)
                .stream()
                .map(t -> t.toString().substring(0, 5))
                .toList();
    }

    @GetMapping("/citas")
    public List<Cita> obtenerCitas() {
        return citaService.obtenerTodas();
    }

    @PostMapping("/citas")
    public Cita crearCita(@RequestBody Cita cita) {
        return citaService.crearCita(cita);
    }
}
