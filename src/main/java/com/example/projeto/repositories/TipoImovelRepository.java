package com.example.projeto.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projeto.models.TipoImovelModel;

public interface TipoImovelRepository extends JpaRepository<TipoImovelModel, Integer> {
    Optional<TipoImovelModel> findFirstByNomeIgnoreCase(String nome);
}