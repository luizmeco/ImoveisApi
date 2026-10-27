package com.example.projeto.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.projeto.models.UserModel;
import com.example.projeto.models.UserRole;
import com.example.projeto.repositories.UserRepository;

@Component
public class InitialAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public InitialAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password,
            @Value("${app.admin.name}") String name) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(String... args) {
        boolean hasEmail = StringUtils.hasText(email);
        boolean hasPassword = StringUtils.hasText(password);
        if (hasEmail != hasPassword) {
            throw new IllegalStateException("Configure ADMIN_EMAIL e ADMIN_PASSWORD juntos para criar o administrador inicial.");
        }
        if (!hasEmail || userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        UserModel admin = new UserModel(null, name, email, passwordEncoder.encode(password),
                UserRole.ADMINISTRADOR.name());
        userRepository.save(admin);
    }
}
