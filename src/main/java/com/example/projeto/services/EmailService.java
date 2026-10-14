package com.example.projeto.services;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service 
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void enviarEmailTexto(String para, String assunto, String corpo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject(assunto);
        mensagem.setText(corpo);

        mailSender.send(mensagem);
    }

    public void enviarEmailComTemplate(
            String para,
            String assunto,
            String template,
            Map<String, Object> variaveis) throws MessagingException {
        Context contexto = new Context();
        contexto.setVariables(variaveis);
        String html = templateEngine.process(template, contexto);

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom(remetente);
        helper.setTo(para);
        helper.setSubject(assunto);
        helper.setText(html, true);

        mailSender.send(mime);
    }
}
