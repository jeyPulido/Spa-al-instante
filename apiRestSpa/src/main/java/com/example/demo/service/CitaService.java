package com.example.demo.service;

import com.example.demo.entity.Cita;
import com.example.demo.repository.CitaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final CalendarioService calendarioService;

    public CitaService(CitaRepository citaRepository, CalendarioService calendarioService) {
        this.citaRepository = citaRepository;
        this.calendarioService = calendarioService;
    }

    public Cita crearCita(Cita cita) {
        return citaRepository.save(cita);
    }

    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }

    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha) {

        if (calendarioService.esDiaCerrado(fecha)) {
            return Collections.emptyList();
        }

        List<Cita> citas = citaRepository.findByFechaHoraBetween(
                fecha.atTime(8, 0),
                fecha.atTime(20, 0)
        );

        List<LocalTime> horarios = new ArrayList<>();

        for (int h = 8; h <= 20; h++) {
            LocalTime hora = LocalTime.of(h, 0);

            long count = citas.stream()
                    .filter(c -> c.getFechaHora().toLocalTime().equals(hora))
                    .count();

            if (count < 2) horarios.add(hora);
        }

        return horarios;
    }
}
