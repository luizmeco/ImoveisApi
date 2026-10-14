package com.example.projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.example.projeto.models.FotoImovelModel;

public interface FotoImovelRepository extends JpaRepository<FotoImovelModel, Integer> {
	List<FotoImovelModel> findByImovel_IdOrderByOrdemAscIdAsc(Integer imovelId);
}
