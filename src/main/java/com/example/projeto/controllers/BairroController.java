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

import com.example.projeto.models.BairroModel;
import com.example.projeto.services.BairroService;

@RestController
@RequestMapping("/bairros")
public class BairroController {

    private final BairroService bairroService;

    public BairroController(BairroService bairroService) {
        this.bairroService = bairroService;
    }

    @GetMapping
    public ResponseEntity<List<BairroModel>> getAllBairros() {
        return ResponseEntity.ok(bairroService.getAll());
    }

    @GetMapping("/bairros-page")
    public ResponseEntity<Page<BairroModel>> getBairrosPage(Pageable pageable) {
        return ResponseEntity.ok(bairroService.getAll(pageable));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<BairroModel> find(@PathVariable Integer id) {
        BairroModel bairro = bairroService.find(id);
        if (bairro == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(bairro);
    }

    @PostMapping
    public ResponseEntity<BairroModel> insert(@RequestBody BairroModel bairro) {
        BairroModel savedBairro = bairroService.insert(bairro);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBairro.getId())
                .toUri();
        return ResponseEntity.created(uri).body(savedBairro);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<BairroModel> update(@PathVariable Integer id, @RequestBody BairroModel bairro) {
        bairro.setId(id);
        BairroModel updatedBairro = bairroService.update(bairro);
        if (updatedBairro == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedBairro);
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (bairroService.delete(id) == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}