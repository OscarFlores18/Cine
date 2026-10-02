package com.app.service;

import java.util.List;

import com.app.dto.EnviarEmailDTO;
import com.app.model.EmailEnviado;

public interface EmailService {
    EmailEnviado enviar(EnviarEmailDTO dto);
    List<EmailEnviado> historial();
}
