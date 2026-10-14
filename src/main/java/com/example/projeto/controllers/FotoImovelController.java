package com.example.projeto.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.projeto.dtos.FotoImovelDTO;
import com.example.projeto.models.FotoImovelModel;
import com.example.projeto.services.FotoImovelService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/fotos")
public class FotoImovelController {

    private final FotoImovelService fotoImovelService;

    public FotoImovelController(FotoImovelService fotoImovelService) {
        this.fotoImovelService = fotoImovelService;
    }

    @GetMapping
    public ResponseEntity<List<FotoImovelDTO>> listar(@RequestParam(required = false) Integer imovelId) {
        return ResponseEntity.ok(fotoImovelService.listar(imovelId).stream().map(FotoImovelDTO::new).toList());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<FotoImovelDTO> buscar(@PathVariable Integer id) {
        return ResponseEntity.ok(new FotoImovelDTO(fotoImovelService.buscar(id)));
    }

    @GetMapping("/{id:\\d+}/arquivo")
    public ResponseEntity<Resource> obterArquivo(@PathVariable Integer id) {
        Path caminho = fotoImovelService.obterArquivo(id);
        String tipoConteudo;
        try {
            tipoConteudo = Files.probeContentType(caminho);
        } catch (IOException e) {
            tipoConteudo = null;
        }
        MediaType mediaType = tipoConteudo == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(tipoConteudo);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + caminho.getFileName() + "\"")
                .body(new FileSystemResource(caminho));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FotoImovelDTO> salvarFoto(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("imovelId") Integer imovelId,
            @RequestParam(required = false) Integer ordem) {
        FotoImovelModel foto = fotoImovelService.salvarFoto(arquivo, imovelId, ordem);
        return ResponseEntity.status(HttpStatus.CREATED).body(new FotoImovelDTO(foto));
    }

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoImovelDTO> atualizar(
            @PathVariable Integer id,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(required = false) Boolean capa,
            @RequestParam(required = false) Integer ordem) {
        return ResponseEntity.ok(new FotoImovelDTO(fotoImovelService.atualizar(id, arquivo, capa, ordem)));
    }

    @PutMapping("/{id:\\d+}/capa")
    public ResponseEntity<FotoImovelDTO> tornarCapa(@PathVariable Integer id) {
        return ResponseEntity.ok(new FotoImovelDTO(fotoImovelService.tornarCapa(id)));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        fotoImovelService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
