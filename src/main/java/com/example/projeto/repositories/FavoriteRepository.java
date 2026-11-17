package com.example.projeto.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projeto.models.FavoriteModel;

public interface FavoriteRepository extends JpaRepository<FavoriteModel, Integer> {
    List<FavoriteModel> findAllByUsuario_EmailIgnoreCase(String email);

    boolean existsByUsuario_IdAndImovel_Id(Integer userId, Integer propertyId);

    void deleteByUsuario_IdAndImovel_Id(Integer userId, Integer propertyId);
}