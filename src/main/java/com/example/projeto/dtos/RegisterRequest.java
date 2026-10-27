package com.example.projeto.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;

public record RegisterRequest(
        String nome,
        String email,
        @JsonAlias("senha") String password,
        @JsonAlias("tipo") String role) {
}
