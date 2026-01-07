package com.example.demo.controller;

import com.example.demo.dto.CitaRequest;
import com.example.demo.dto.HorarioDTO;
import com.example.demo.entity.Cita;
import com.example.demo.entity.EstadoCita;
import com.example.demo.service.CitaService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class CitaController {

    private final CitaService citaService;


    public CitaController(CitaService citaService) {
        this.citaService = citaService;

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
    @PatchMapping("/citas/{id}/estado")
    public Cita cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoCita estado
    ) {
        return citaService.cambiarEstado(id, estado);
    }

    @DeleteMapping("/citas/{id}")
    public void cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
    }
    @GetMapping("/citas/usuario/{id}")
    public List<Cita> obtenerCitasPorUsuario(@PathVariable Long id) {
        return citaService.obtenerCitasPorUsuario(id);
    }
    @PatchMapping("/citas/{id}/reagendar")
    public Cita reagendarCita(
            @PathVariable Long id,
            @RequestParam String nuevaFechaHora
    ) {
        return citaService.reagendarCita(id, nuevaFechaHora);
    }

}
