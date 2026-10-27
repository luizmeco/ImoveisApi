package com.example.projeto.models;

public enum UserRole {
    ADMINISTRADOR,
    CORRETOR;

    public String authority() {
        return "ROLE_" + (this == ADMINISTRADOR ? "ADMIN" : "CORRETOR");
    }

    public static UserRole fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Perfil de usuário obrigatório.");
        }

        return switch (value.trim().toUpperCase()) {
            case "ADMIN", "ADMINISTRADOR" -> ADMINISTRADOR;
            case "CORRETOR" -> CORRETOR;
            default -> throw new IllegalArgumentException("Perfil de usuário inválido.");
        };
    }
}
