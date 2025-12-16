package com.example.demo.service;

import com.example.demo.entity.Cita;

import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoConfirmacion(Cita cita) {

        String html = """
        <html>
        <body style="font-family: Arial; background-color:#f5f5f5; padding:20px">
            <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:20px">
                <h2 style="color:#8e44ad;text-align:center">🌿 Spa al Instante 🌿</h2>

                <p>Hola <strong>%s</strong>,</p>

                <p>Tu cita ha sido <strong style="color:green">confirmada</strong> con éxito.</p>

                <div style="background:#f0f0f0;padding:15px;border-radius:8px">
                    <p><strong>📅 Fecha:</strong> %s</p>
                    <p><strong>💆 Servicio:</strong> %s</p>
                    <p><strong>📞 Teléfono:</strong> %s</p>
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
                cita.getServicio().getNombre(),
                cita.getTelefono()
        );

        enviarHtml(cita.getCorreo(), "Confirmación de tu cita", html);
    }

    public void enviarCorreoCancelacion(Cita cita) {

        String html = """
        <html>
        <body style="font-family: Arial">
            <h2 style="color:red">Cita cancelada</h2>
            <p>Hola %s,</p>
            <p>Tu cita para el servicio <strong>%s</strong> ha sido cancelada.</p>
            <p>Esperamos verte pronto 💖</p>
        </body>
        </html>
        """.formatted(
                cita.getNombreCliente(),
                cita.getServicio().getNombre()
        );

        enviarHtml(cita.getCorreo(), "Cita cancelada", html);
    }

    private void enviarHtml(String to, String subject, String html) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
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

