package com.example.projeto.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.projeto.models.TipoImovelModel;
import com.example.projeto.repositories.TipoImovelRepository;

@Service
public class TipoImovelService {

    private final TipoImovelRepository tipoImovelRepository;

    public TipoImovelService(TipoImovelRepository tipoImovelRepository) {
        this.tipoImovelRepository = tipoImovelRepository;
    }

    public List<TipoImovelModel> getAll() {
        return tipoImovelRepository.findAll();
    }

    public Page<TipoImovelModel> getAll(Pageable pageable) {
        return tipoImovelRepository.findAll(pageable);
    }

    public TipoImovelModel find(Integer id) {
        return tipoImovelRepository.findById(id).orElse(null);
    }

    public TipoImovelModel insert(TipoImovelModel tipoImovel) {
        return tipoImovelRepository.save(tipoImovel);
    }

    public TipoImovelModel update(TipoImovelModel tipoImovel) {
        TipoImovelModel existingTipoImovel = find(tipoImovel.getId());
        if (existingTipoImovel == null) {
            return null;
        }

        existingTipoImovel.setNome(tipoImovel.getNome());
        existingTipoImovel.setDescricao(tipoImovel.getDescricao());
        return tipoImovelRepository.save(existingTipoImovel);
    }

    public TipoImovelModel delete(Integer id) {
        TipoImovelModel tipoImovel = find(id);
        if (tipoImovel == null) {
            return null;
        }

        tipoImovelRepository.deleteById(id);
        return tipoImovel;
    }
}