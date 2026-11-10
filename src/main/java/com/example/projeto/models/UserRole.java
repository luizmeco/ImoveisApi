package com.example.projeto.models;

public enum UserRole {
    ADMINISTRADOR,
    CORRETOR,
    USER;

    public String authority() {
        return switch (this) {
            case ADMINISTRADOR -> "ROLE_ADMIN";
            case CORRETOR -> "ROLE_CORRETOR";
            case USER -> "ROLE_USER";
        };
    }

    public static UserRole fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Perfil de usuário obrigatório.");
        }

        return switch (value.trim().toUpperCase()) {
            case "ADMIN", "ADMINISTRADOR" -> ADMINISTRADOR;
            case "CORRETOR" -> CORRETOR;
            case "USER", "CLIENTE" -> USER;
            default -> throw new IllegalArgumentException("Perfil de usuário inválido.");
        };
    }
}
