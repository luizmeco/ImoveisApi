package com.example.projeto.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.projeto.models.FavoriteModel;
import com.example.projeto.repositories.FavoriteRepository;
import com.example.projeto.repositories.ImovelRepository;
import com.example.projeto.repositories.UserRepository;

@RestController
@RequestMapping("/favoritos")
public class FavoriteController {
    private final FavoriteRepository favoriteRepository;
    private final ImovelRepository imovelRepository;
    private final UserRepository userRepository;

    public FavoriteController(
            FavoriteRepository favoriteRepository,
            ImovelRepository imovelRepository,
            UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.imovelRepository = imovelRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<FavoriteModel> list(Principal principal) {
        return favoriteRepository.findAllByUsuario_EmailIgnoreCase(principal.getName());
    }

    @PostMapping
    public ResponseEntity<FavoriteModel> add(@RequestBody FavoriteRequest request, Principal principal) {
        var user = userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        var property = imovelRepository.findById(request.imovelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));
        if (favoriteRepository.existsByUsuario_IdAndImovel_Id(user.getId(), property.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este imóvel já está nos seus favoritos.");
        }
        FavoriteModel favorite = new FavoriteModel();
        favorite.setUsuario(user);
        favorite.setImovel(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteRepository.save(favorite));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> remove(@PathVariable Integer propertyId, Principal principal) {
        var user = userRepository.findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (!favoriteRepository.existsByUsuario_IdAndImovel_Id(user.getId(), propertyId)) {
            return ResponseEntity.notFound().build();
        }
        favoriteRepository.deleteByUsuario_IdAndImovel_Id(user.getId(), propertyId);
        return ResponseEntity.noContent().build();
    }

    public record FavoriteRequest(Integer imovelId) {}
}