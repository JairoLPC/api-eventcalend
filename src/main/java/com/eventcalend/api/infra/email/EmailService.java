package com.eventcalend.api.infra.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoRecuperacao(String paraEmail, String codigo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("suporte@eventcalend.com");
            message.setTo(paraEmail);
            message.setSubject("EVENTCALEND - Código de Verificação para Redefinição de Senha");
            message.setText("Olá!\n\n"
                    + "Seu código de verificação para redefinir sua senha no EVENTCALEND é:\n\n"
                    + "👉 " + codigo + "\n\n"
                    + "Este código de segurança é válido por 15 minutos.\n"
                    + "Se você não solicitou a alteração de senha, por favor desconsidere este e-mail.\n\n"
                    + "Atenciosamente,\n"
                    + "Equipe EVENTCALEND");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Aviso: Erro ao enviar e-mail de recuperação para " + paraEmail + ": " + e.getMessage());
        }
    }
}
