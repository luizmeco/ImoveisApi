package com.example.projeto.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.projeto.services.EmailService;

@RestController 
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<String> enviar(@RequestParam String para) {
        try {
            emailService.enviarEmailComTemplate(
                    para,
                    "Bem-vindo!",
                    "email/template",
                    Map.of(
                            "nome", "Maria",
                            "itens", List.of("Plano Pro", "Suporte 24h"),
                            "link", "https://meusite.com"));
            return ResponseEntity.ok("Enviado");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
