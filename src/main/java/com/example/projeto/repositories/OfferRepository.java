package com.example.projeto.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projeto.models.OfferModel;

public interface OfferRepository extends JpaRepository<OfferModel, Integer> {
    List<OfferModel> findAllByUsuario_EmailIgnoreCaseOrderByCreatedAtDesc(String email);

    List<OfferModel> findAllByOrderByCreatedAtDesc();

    List<OfferModel> findAllByImovel_IdAndStatusIgnoreCase(Integer imovelId, String status);

    boolean existsByImovel_IdAndUsuario_IdAndStatusIgnoreCase(Integer imovelId, Integer usuarioId, String status);
}