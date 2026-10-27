package com.example.projeto.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LoginRequest(String email, @JsonAlias("senha") String password) {
}
