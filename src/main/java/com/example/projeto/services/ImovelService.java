package com.example.projeto.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.projeto.models.ImovelModel;
import com.example.projeto.repositories.ImovelRepository;

@Service
public class ImovelService {

    private final ImovelRepository imovelRepository;

    public ImovelService(ImovelRepository imovelRepository) {
        this.imovelRepository = imovelRepository;
    }

    public List<ImovelModel> getAll() {
        return imovelRepository.findAll();
    }

    public ImovelModel find(Integer id) {
        return imovelRepository.findById(id).orElse(null);
    }

    public ImovelModel insert(ImovelModel imovel) {
        return imovelRepository.save(imovel);
    }

    public ImovelModel update(Integer id, ImovelModel imovel) {
        ImovelModel existingImovel = find(id);
        if (existingImovel == null) {
            return null;
        }

        imovel.setId(id);
        imovel.setCreatedAt(existingImovel.getCreatedAt());
        return imovelRepository.save(imovel);
    }

    public ImovelModel updateStatus(Integer id, String status) {
        ImovelModel imovel = find(id);
        if (imovel == null) {
            return null;
        }
        imovel.setStatus(status);
        return imovelRepository.save(imovel);
    }

    public boolean delete(Integer id) {
        if (!imovelRepository.existsById(id)) {
            return false;
        }

        imovelRepository.deleteById(id);
        return true;
    }
}