package com.example.projeto.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projeto.models.ContractModel;

public interface ContractRepository extends JpaRepository<ContractModel, Integer> {
    List<ContractModel> findAllByOferta_Usuario_EmailIgnoreCaseOrderByDataCriacaoDesc(String email);

    List<ContractModel> findAllByOrderByDataCriacaoDesc();

    Optional<ContractModel> findByOferta_Id(Integer offerId);
}