package com.example.projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projeto.models.TipoImovelModel;

public interface TipoImovelRepository extends JpaRepository<TipoImovelModel, Integer> {
}