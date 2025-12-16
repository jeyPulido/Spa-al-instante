package com.example.demo.service;

import com.example.demo.dto.CitaRequest;
import com.example.demo.dto.HorarioDTO;
import com.example.demo.entity.Cita;
import com.example.demo.entity.Servicio;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.ServicioRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final CalendarioService calendarioService;
    private final EmailService emailService;

    public CitaService(
            CitaRepository citaRepository,
            ServicioRepository servicioRepository,
            CalendarioService calendarioService,
            EmailService emailService
    ) {
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.calendarioService = calendarioService;
        this.emailService = emailService;
    }

    /* ===============================
       CREAR CITA
    =============================== */
    public Cita crearCita(CitaRequest request) {

        Servicio servicio = servicioRepository.findById(request.servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        Cita cita = new Cita();
        cita.setNombreCliente(request.nombreCliente);
        cita.setApellidosCliente(request.apellidosCliente);
        cita.setCorreo(request.correo);
        cita.setTelefono(request.telefono);
        cita.setFechaHora(request.fechaHora);
        cita.setServicio(servicio);

        Cita citaGuardada = citaRepository.save(cita);

        emailService.enviarCorreoConfirmacion(citaGuardada);

        return citaGuardada;
    }

    /* ===============================
       CANCELAR
    =============================== */
    public void cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        citaRepository.deleteById(id);
        emailService.enviarCorreoCancelacion(cita);
    }

    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }

    /* ===============================
       HORARIOS DISPONIBLES (DTO)
    =============================== */
    public List<HorarioDTO> obtenerHorariosDisponibles(LocalDate fecha) {

        // ❌ Día cerrado (domingos, festivos, +2 meses)
        if (calendarioService.esDiaCerrado(fecha)) {
            return List.of();
        }

        List<Cita> citas = citaRepository.findByFechaHoraBetween(
                fecha.atTime(8, 0),
                fecha.atTime(20, 0)
        );

        List<HorarioDTO> resultado = new ArrayList<>();

        for (int h = 8; h <= 20; h++) {
            LocalTime hora = LocalTime.of(h, 0);

            long count = citas.stream()
                    .filter(c -> c.getFechaHora().toLocalTime().equals(hora))
                    .count();

            boolean disponible = count < 2;

            resultado.add(
                    new HorarioDTO(
                            hora.toString().substring(0, 5),
                            disponible
                    )
            );
        }

        return resultado;
    }

    public List<LocalDate> obtenerDiasSaturados() {

        Map<LocalDate, Long> conteo = citaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getFechaHora().toLocalDate(),
                        Collectors.counting()
                ));

        int maxPorDia = (20 - 8 + 1) * 2; // 2 citas por hora

        return conteo.entrySet().stream()
                .filter(e -> e.getValue() >= maxPorDia)
                .map(Map.Entry::getKey)
                .toList();
    }
}
