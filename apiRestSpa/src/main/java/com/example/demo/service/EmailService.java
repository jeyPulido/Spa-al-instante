package com.example.demo.service;

import com.example.demo.entity.Cita;
import com.example.demo.entity.Servicio;
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

    /* ===============================
       CONFIRMACIÓN DE CITA
    =============================== */
    public void enviarCorreoConfirmacion(Cita cita) {

        // 🔥 LISTA DE SERVICIOS
        String serviciosHtml = cita.getServicios()
                .stream()
                .map(s -> "• " + s.getNombre())
                .collect(Collectors.joining("<br>"));

        // 🔥 LINK DE CANCELACIÓN
        String cancelarUrl = "http://localhost:9000/cancelar-cita/" + cita.getId();

        String html = """
        <html>
        <body style="font-family: Arial; background-color:#f5f5f5; padding:20px">
            <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:20px">

                <h2 style="color:#8e44ad;text-align:center">🌿 Spa al Instante 🌿</h2>

                <p>Hola <strong>%s</strong>,</p>

                <p>Tu cita ha sido <strong style="color:green">confirmada</strong> con éxito.</p>

                <div style="background:#f0f0f0;padding:15px;border-radius:8px">
                    <p><strong>📅 Fecha:</strong> %s</p>
                    <p><strong>💆 Servicios:</strong><br>%s</p>
                    <p><strong>📞 Teléfono:</strong> %s</p>
                </div>

                <!-- BOTÓN CANCELAR -->
                <div style="text-align:center;margin-top:25px">
                    <a href="%s"
                       style="background:#e74c3c;color:white;padding:12px 20px;
                              text-decoration:none;border-radius:6px;font-weight:bold">
                        ❌ Cancelar cita
                    </a>
                </div>

                <p style="margin-top:20px">
                    Gracias por confiar en nosotros 💖<br>
                    <strong>Spa al Instante</strong>
                </p>

            </div>
        </body>
        </html>
        """.formatted(
                cita.getNombreCliente(),
                cita.getFechaHora(),
                serviciosHtml,
                cita.getTelefono(),
                cancelarUrl
        );

        enviarHtml(cita.getCorreo(), "Confirmación de tu cita", html);
    }

    /* ===============================
       CORREO DE CANCELACIÓN
    =============================== */
    public void enviarCorreoCancelacion(Cita cita) {

        String servicios = cita.getServicios()
                .stream()
                .map(Servicio::getNombre)
                .collect(Collectors.joining(", "));

        String html = """
        <html>
        <body style="font-family: Arial">
            <h2 style="color:red">❌ Cita cancelada</h2>
            <p>Hola %s,</p>
            <p>Tu cita para los servicios <strong>%s</strong> ha sido cancelada.</p>
            <p>Esperamos verte pronto 💖</p>
        </body>
        </html>
        """.formatted(
                cita.getNombreCliente(),
                servicios
        );

        enviarHtml(cita.getCorreo(), "Cita cancelada", html);
    }

    /* ===============================
       MÉTODO BASE
    =============================== */
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
