package com.example.projeto.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projeto.models.BairroModel;

public interface BairroRepository extends JpaRepository<BairroModel, Integer> {
    Optional<BairroModel> findFirstByNomeIgnoreCaseAndCidadeIgnoreCase(String nome, String cidade);
}