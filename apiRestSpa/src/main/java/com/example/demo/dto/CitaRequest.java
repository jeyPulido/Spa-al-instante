package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CitaRequest {

	public String nombreCliente;
	public String apellidosCliente;
	public String correo;
	public String telefono;
	public LocalDateTime fechaHora;

	public List<Long> serviciosIds;

	public Long usuarioId;
}
