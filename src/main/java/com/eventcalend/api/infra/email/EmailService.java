package com.eventcalend.api.infra.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.username:suporte@eventcalend.com}") String remetente
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.remetente = remetente;
    }

    public void enviarCodigoRecuperacao(String paraEmail, String codigo) {
        if (mailSender == null) {
            log.warn("Aviso: JavaMailSender não configurado. Código gerado para {}: {}", paraEmail, codigo);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(paraEmail);
            message.setSubject("EVENTCALEND - Código de Verificação para Redefinição de Senha");
            message.setText("""
                    Olá!

                    Seu código de verificação para redefinir sua senha no EVENTCALEND é:

                    👉 %s

                    Este código de segurança é válido por 15 minutos.
                    Se você não solicitou a alteração de senha, por favor desconsidere este e-mail.

                    Atenciosamente,
                    Equipe EVENTCALEND""".formatted(codigo));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Aviso: Erro ao enviar e-mail de recuperação para {}: {}", paraEmail, e.getMessage());
        }
    }
}
