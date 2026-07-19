package com.mediturno.mediturno.modules.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarConfirmacionReserva(String destinatario, String pacienteNombre, String medicoNombre, String fecha, String hora) {
        log.info("Enviando email asíncrono de confirmación de reserva a {}", destinatario);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(destinatario);
            helper.setSubject("Confirmación de Reserva de Turno - MediTurno");
            
            String htmlContent = String.format(
                    "<html>" +
                    "<body>" +
                    "  <h2>Hola %s,</h2>" +
                    "  <p>Te confirmamos que se ha reservado tu turno correctamente.</p>" +
                    "  <p><strong>Médico:</strong> %s</p>" +
                    "  <p><strong>Fecha:</strong> %s</p>" +
                    "  <p><strong>Hora:</strong> %s</p>" +
                    "  <br/>" +
                    "  <p>Saludos cordiales,<br/>El equipo de MediTurno</p>" +
                    "</body>" +
                    "</html>",
                    pacienteNombre, medicoNombre, fecha, hora
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email de confirmación de reserva enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.error("Fallo al enviar correo de confirmación de reserva a {}: {}", destinatario, e.getMessage());
        }
    }

    @Async
    public void enviarConfirmacionCancelacion(String destinatario, String pacienteNombre, String medicoNombre, String fecha, String hora) {
        log.info("Enviando email asíncrono de confirmación de cancelación a {}", destinatario);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(destinatario);
            helper.setSubject("Cancelación de Reserva de Turno - MediTurno");
            
            String htmlContent = String.format(
                    "<html>" +
                    "<body>" +
                    "  <h2>Hola %s,</h2>" +
                    "  <p>Te informamos que tu turno médico programado ha sido cancelado.</p>" +
                    "  <p><strong>Médico:</strong> %s</p>" +
                    "  <p><strong>Fecha:</strong> %s</p>" +
                    "  <p><strong>Hora:</strong> %s</p>" +
                    "  <br/>" +
                    "  <p>Saludos cordiales,<br/>El equipo de MediTurno</p>" +
                    "</body>" +
                    "</html>",
                    pacienteNombre, medicoNombre, fecha, hora
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email de confirmación de cancelación enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.error("Fallo al enviar correo de confirmación de cancelación a {}: {}", destinatario, e.getMessage());
        }
    }
}
