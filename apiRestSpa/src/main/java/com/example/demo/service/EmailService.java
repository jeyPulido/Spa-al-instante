package com.example.demo.service;

import com.example.demo.entity.Cita;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class EmailService {

	private final JavaMailSender mailSender;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void enviarCorreoPorEstado(Cita cita) {
		switch (cita.getEstado()) {
		case PENDIENTE -> enviarCorreoPendiente(cita);
		case CONFIRMADA -> enviarCorreoConfirmada(cita);
		case CANCELADA -> enviarCorreoCancelada(cita);
		case REAGENDADA -> enviarCorreoReagendada(cita);
		case ATENDIDA -> enviarCorreoAtendida(cita);
		}
	}

	private String plantillaBase(String titulo, String cuerpoHtml) {
		return """
				<html>
				<body style="font-family:Arial;background:#f5f5f5;padding:20px">
				    <div style="max-width:600px;margin:auto;background:white;
				                border-radius:12px;padding:25px">

				        <h2 style="text-align:center;color:#8e44ad">
				            🌿 Spa al Instante 🌿
				        </h2>

				        <h3 style="text-align:center">%s</h3>

				        %s

				        <hr style="margin:30px 0">

				        <p style="text-align:center;font-size:14px;color:#777">
				            Gracias por confiar en nosotros 💖<br>
				            <strong>Spa al Instante</strong>
				        </p>

				    </div>
				</body>
				</html>
				""".formatted(titulo, cuerpoHtml);
	}

	private String serviciosHtml(Cita cita) {
		return cita.getServicios().stream().map(s -> "• " + s.getNombre()).collect(Collectors.joining("<br>"));
	}

	private void enviarCorreoPendiente(Cita cita) {

		String cuerpo = """
				<p>Hola <strong>%s</strong>,</p>

				<p>Hemos recibido tu solicitud de cita 🧘‍♀️</p>

				<p><strong>Estado actual:</strong>
				   <span style="color:#f39c12">PENDIENTE DE CONFIRMACIÓN</span>
				</p>

				<div style="background:#f0f0f0;padding:15px;border-radius:8px">
				    <p><strong>📅 Fecha:</strong> %s</p>
				    <p><strong>💆 Servicios:</strong><br>%s</p>
				</div>

				<p>Te avisaremos por este medio cuando el spa confirme tu cita.</p>
				""".formatted(cita.getNombreCliente(), cita.getFechaHora(), serviciosHtml(cita));

		enviarHtml(cita.getCorreo(), "Hemos recibido tu cita - Spa al Instante",
				plantillaBase("Cita recibida", cuerpo));
	}

	private void enviarCorreoConfirmada(Cita cita) {

		String cancelarUrl = "http://localhost:9000/cancelar-cita/" + cita.getId();

		String cuerpo = """
				<p>Hola <strong>%s</strong>,</p>

				<p>Tu cita ha sido
				   <strong style="color:green">CONFIRMADA</strong> ✅</p>

				<div style="background:#f0f0f0;padding:15px;border-radius:8px">
				    <p><strong>📅 Fecha:</strong> %s</p>
				    <p><strong>💆 Servicios:</strong><br>%s</p>
				    <p><strong>📞 Teléfono:</strong> %s</p>
				</div>

				<div style="text-align:center;margin-top:25px">
				    <a href="%s"
				       style="background:#e74c3c;color:white;
				              padding:12px 20px;border-radius:6px;
				              text-decoration:none;font-weight:bold">
				        ❌ Cancelar cita
				    </a>
				</div>
				""".formatted(cita.getNombreCliente(), cita.getFechaHora(), serviciosHtml(cita), cita.getTelefono(),
				cancelarUrl);

		enviarHtml(cita.getCorreo(), "Tu cita ha sido confirmada - Spa al Instante",
				plantillaBase("Cita confirmada", cuerpo));
	}

	private void enviarCorreoCancelada(Cita cita) {

		String cuerpo = """
				<p>Hola <strong>%s</strong>,</p>

				<p>Tu cita ha sido
				   <strong style="color:red">CANCELADA</strong> ❌</p>

				<p>Si deseas agendar una nueva cita,
				   estaremos encantados de atenderte nuevamente.</p>
				""".formatted(cita.getNombreCliente());

		enviarHtml(cita.getCorreo(), "Tu cita fue cancelada - Spa al Instante",
				plantillaBase("Cita cancelada", cuerpo));
	}

	private void enviarCorreoReagendada(Cita cita) {

		String cuerpo = """
				<p>Hola <strong>%s</strong>,</p>

				<p>Tu cita ha sido
				   <strong style="color:#2980b9">REAGENDADA</strong> 🔄</p>

				<div style="background:#f0f0f0;padding:15px;border-radius:8px">
				    <p><strong>📅 Nueva fecha:</strong> %s</p>
				    <p><strong>💆 Servicios:</strong><br>%s</p>
				</div>
				""".formatted(cita.getNombreCliente(), cita.getFechaHora(), serviciosHtml(cita));

		enviarHtml(cita.getCorreo(), "Tu cita fue reagendada - Spa al Instante",
				plantillaBase("Cita reagendada", cuerpo));
	}

	private void enviarCorreoAtendida(Cita cita) {

		String cuerpo = """
				<p>Hola <strong>%s</strong>,</p>

				<p>Fue un placer atenderte 💆‍♀️✨</p>

				<p>Esperamos que hayas disfrutado tu experiencia en
				   <strong>Spa al Instante</strong>.</p>

				<p>¡Te esperamos pronto!</p>
				""".formatted(cita.getNombreCliente());

		enviarHtml(cita.getCorreo(), "Gracias por tu visita - Spa al Instante",
				plantillaBase("Gracias por visitarnos", cuerpo));
	}

	private void enviarHtml(String to, String subject, String html) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(html, true);

			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
