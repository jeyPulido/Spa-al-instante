package com.example.demo.service;

import com.example.demo.dto.CitaRequest;
import com.example.demo.dto.HorarioDTO;
import com.example.demo.entity.Cita;
import com.example.demo.entity.EstadoCita;
import com.example.demo.entity.Servicio;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.ServicioRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CalendarioService calendarioService;
    private final EmailService emailService;

    public CitaService(
            CitaRepository citaRepository,
            ServicioRepository servicioRepository,
            UsuarioRepository usuarioRepository,
            CalendarioService calendarioService,
            EmailService emailService
    ) {
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.calendarioService = calendarioService;
        this.emailService = emailService;
    }

    /* ===============================
       CREAR CITA
    =============================== */
    public Cita crearCita(CitaRequest request) {

        Cita cita = new Cita();
        cita.setNombreCliente(request.nombreCliente);
        cita.setApellidosCliente(request.apellidosCliente);
        cita.setCorreo(request.correo);
        cita.setTelefono(request.telefono);
        cita.setFechaHora(request.fechaHora);

        // ✅ USUARIO
        if (request.usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(request.usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            cita.setUsuario(usuario);
        }

        // ✅ SERVICIOS
        List<Servicio> servicios = servicioRepository
                .findAllById(request.serviciosIds);

        cita.setServicios(servicios);

        // ✅ GUARDAR
        Cita citaGuardada = citaRepository.save(cita);

        // 🔥 EMAIL CONFIRMACIÓN
        emailService.enviarCorreoConfirmacion(citaGuardada);

        return citaGuardada;
    }

    /* ===============================
       CANCELAR CITA
    =============================== */
    public void cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);

        emailService.enviarCorreoCancelacion(cita);
    }

    public Cita cambiarEstado(Long id, EstadoCita estado) {
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado(estado);
        return citaRepository.save(cita);
    }

    /* ===============================
       LISTAR TODAS
    =============================== */
    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }

    /* ===============================
       HISTORIAL POR USUARIO
    =============================== */
    public List<Cita> obtenerCitasPorUsuario(Long usuarioId) {
        return citaRepository.findByUsuario_IdOrderByFechaHoraDesc(usuarioId);
    }

    /* ===============================
       HORARIOS DISPONIBLES
    =============================== */
    public List<HorarioDTO> obtenerHorariosDisponibles(LocalDate fecha) {

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

            resultado.add(new HorarioDTO(
                    hora.toString().substring(0, 5),
                    disponible
            ));
        }

        return resultado;
    }

    /* ===============================
       DÍAS SATURADOS
    =============================== */
    public List<LocalDate> obtenerDiasSaturados() {

        Map<LocalDate, Long> conteo = citaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getFechaHora().toLocalDate(),
                        Collectors.counting()
                ));

        int maxPorDia = (20 - 8 + 1) * 2;

        return conteo.entrySet().stream()
                .filter(e -> e.getValue() >= maxPorDia)
                .map(Map.Entry::getKey)
                .toList();
    }
}
