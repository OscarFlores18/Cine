package com.app.controller;

import com.app.dto.EnviarEmailDTO;
import com.app.model.EmailEnviado;
import com.app.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gmail")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<EmailEnviado> enviar(@Valid @RequestBody EnviarEmailDTO dto) {
        return ResponseEntity.ok(emailService.enviar(dto));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<EmailEnviado>> historial() {
        return ResponseEntity.ok(emailService.historial());
    }
}
