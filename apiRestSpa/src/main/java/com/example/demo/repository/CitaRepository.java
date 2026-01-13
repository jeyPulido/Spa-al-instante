package com.example.demo.repository;

import com.example.demo.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

	List<Cita> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

	List<Cita> findByUsuario_IdOrderByFechaHoraDesc(Long usuarioId);
}
