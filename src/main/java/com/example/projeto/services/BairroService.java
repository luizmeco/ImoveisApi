package com.example.projeto.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.projeto.models.BairroModel;
import com.example.projeto.repositories.BairroRepository;

@Service
public class BairroService {

    private final BairroRepository bairroRepository;

    public BairroService(BairroRepository bairroRepository) {
        this.bairroRepository = bairroRepository;
    }

    public List<BairroModel> getAll() {
        return bairroRepository.findAll();
    }

    public Page<BairroModel> getAll(Pageable pageable) {
        return bairroRepository.findAll(pageable);
    }

    public BairroModel find(Integer id) {
        return bairroRepository.findById(id).orElse(null);
    }

    public BairroModel insert(BairroModel bairro) {
        return bairroRepository.save(bairro);
    }

    public BairroModel update(BairroModel bairro) {
        BairroModel existingBairro = find(bairro.getId());
        if (existingBairro == null) {
            return null;
        }

        existingBairro.setNome(bairro.getNome());
        existingBairro.setCidade(bairro.getCidade());
        existingBairro.setEstado(bairro.getEstado());
        existingBairro.setCepInicial(bairro.getCepInicial());
        existingBairro.setCepFinal(bairro.getCepFinal());
        return bairroRepository.save(existingBairro);
    }

    public BairroModel delete(Integer id) {
        BairroModel bairro = find(id);
        if (bairro == null) {
            return null;
        }

        bairroRepository.deleteById(id);
        return bairro;
    }
}