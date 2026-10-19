package com.app.service;

import com.app.dto.EnviarEmailDTO;
import com.app.model.EmailEnviado;
import com.app.repository.EmailEnviadoRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailEnviadoRepository emailEnviadoRepository;

    // Cuenta de Gmail desde la que se manda (spring.mail.username)
    @Value("${spring.mail.username:}")
    private String remitente;

    @Override
    public EmailEnviado enviar(EnviarEmailDTO dto) {
        return enviar(dto, null);
    }

    @Override
    public EmailEnviado enviar(EnviarEmailDTO dto, Long ventaId) {
        return enviar(dto, ventaId, "CONFIRMACION");
    }

    @Override
    public EmailEnviado enviar(EnviarEmailDTO dto, Long ventaId, String tipo) {
        EmailEnviado registro = new EmailEnviado();
        registro.setVentaId(ventaId);
        registro.setTipo(tipo);
        registro.setDestinatario(dto.destinatario());
        registro.setAsunto(dto.asunto());
        registro.setFecha(LocalDateTime.now());
        registro.setEstado("PENDIENTE");

        // Se guarda primero (PENDIENTE) y recien despues se manda el mail: si
        // el mail sale y luego falla el guardado, igual queda registro de que
        // se intento enviar, y el historial nunca miente.
        registro = emailEnviadoRepository.save(registro);

        try {
            // El cuerpo llega como HTML (ticket con estilo + QR incrustado como
            // data URI); por eso se manda como MimeMessage y no como el viejo
            // SimpleMailMessage (que solo soporta texto plano).
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            if (remitente != null && !remitente.isBlank()) {
                helper.setFrom(remitente);
            }
            helper.setTo(dto.destinatario());
            helper.setSubject(dto.asunto());
            helper.setText(dto.mensaje(), true);
            mailSender.send(mimeMessage);
            registro.setEstado("ENVIADO");
        } catch (MailException | jakarta.mail.MessagingException e) {
            // Si Gmail rechaza (credenciales, red, etc.) se guarda el error y se
            // devuelve el registro, en vez de romper todo el flujo.
            log.error("No se pudo enviar el mail a {}: {}", dto.destinatario(), e.getMessage());
            registro.setEstado("ERROR");
            registro.setDetalleError(e.getMessage());
        }

        return emailEnviadoRepository.save(registro);
    }

    @Override
    public List<EmailEnviado> historial() {
        return emailEnviadoRepository.findAll();
    }
}
