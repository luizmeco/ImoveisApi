package com.example.projeto.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.projeto.models.TipoImovelModel;
import com.example.projeto.services.TipoImovelService;

@RestController
@RequestMapping("/tipos-imoveis")
public class TipoImovelController {

    private final TipoImovelService tipoImovelService;

    public TipoImovelController(TipoImovelService tipoImovelService) {
        this.tipoImovelService = tipoImovelService;
    }

    @GetMapping
    public ResponseEntity<List<TipoImovelModel>> getAllTiposImoveis() {
        return ResponseEntity.ok(tipoImovelService.getAll());
    }

    @GetMapping("/tipos-imoveis-page")
    public ResponseEntity<Page<TipoImovelModel>> getTiposImoveisPage(Pageable pageable) {
        return ResponseEntity.ok(tipoImovelService.getAll(pageable));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<TipoImovelModel> find(@PathVariable Integer id) {
        TipoImovelModel tipoImovel = tipoImovelService.find(id);
        if (tipoImovel == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tipoImovel);
    }

    @PostMapping
    public ResponseEntity<TipoImovelModel> insert(@RequestBody TipoImovelModel tipoImovel) {
        TipoImovelModel savedTipoImovel = tipoImovelService.insert(tipoImovel);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedTipoImovel.getId())
                .toUri();
        return ResponseEntity.created(uri).body(savedTipoImovel);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<TipoImovelModel> update(
            @PathVariable Integer id,
            @RequestBody TipoImovelModel tipoImovel) {
        tipoImovel.setId(id);
        TipoImovelModel updatedTipoImovel = tipoImovelService.update(tipoImovel);
        if (updatedTipoImovel == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedTipoImovel);
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (tipoImovelService.delete(id) == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}