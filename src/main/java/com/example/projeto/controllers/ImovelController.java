package com.example.projeto.controllers;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.example.projeto.dtos.SolicitacaoImovelDTO;
import com.example.projeto.models.ImovelModel;
import com.example.projeto.services.EmailService;
import com.example.projeto.services.ImovelService;

@RestController
@RequestMapping("/imoveis")
public class ImovelController {

    private final ImovelService imovelService;
    private final EmailService emailService;

    public ImovelController(ImovelService imovelService, EmailService emailService) {
        this.imovelService = imovelService;
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<List<ImovelModel>> getAll() {
        return ResponseEntity.ok(imovelService.getAll());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ImovelModel> find(@PathVariable Integer id) {
        ImovelModel imovel = imovelService.find(id);
        if (imovel == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(imovel);
    }

    @PostMapping("/sabermais/{id:\\d+}")
    public ResponseEntity<String> saberMais(
            @PathVariable Integer id,
            @RequestBody SolicitacaoImovelDTO solicitacao) {
        ImovelModel imovel = imovelService.find(id);
        if (imovel == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("nome", solicitacao.getNome());
        variaveis.put("telefone", solicitacao.getTelefone());
        variaveis.put("email", solicitacao.getEmail());
        variaveis.put("mensagem", solicitacao.getMensagem());
        variaveis.put("titulo", imovel.getTitulo());
        variaveis.put("descricao", imovel.getDescricao());
        variaveis.put("finalidade", imovel.getFinalidade());
        variaveis.put("status", imovel.getStatus());
        variaveis.put("precoVenda", imovel.getPrecoVenda());
        variaveis.put("precoAluguel", imovel.getPrecoAluguel());
        variaveis.put("dormitorios", imovel.getDormitorios());
        variaveis.put("banheiros", imovel.getBanheiros());
        variaveis.put("garagem", imovel.getGaragem());
        variaveis.put("areaTotal", imovel.getAreaTotal());
        variaveis.put("areaConstruida", imovel.getAreaConstruida());
        variaveis.put("endereco", imovel.getEndereco());
        variaveis.put("numero", imovel.getNumero());
        variaveis.put("complemento", imovel.getComplemento());
        variaveis.put("cep", imovel.getCep());
        variaveis.put("caracteristicas", imovel.getCaracteristicas());

        try {
            emailService.enviarEmailComTemplate(
                    solicitacao.getEmail(),
                    "Mais informações sobre: " + imovel.getTitulo(),
                    "email/imovel-interesse",
                    variaveis);
            return ResponseEntity.ok("Informações do imóvel enviadas por e-mail.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Não foi possível enviar o e-mail.");
        }
    }

    @PostMapping
    public ResponseEntity<ImovelModel> insert(@RequestBody ImovelModel imovel) {
        ImovelModel savedImovel = imovelService.insert(imovel);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedImovel.getId())
                .toUri();
        return ResponseEntity.created(uri).body(savedImovel);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ImovelModel> update(
            @PathVariable Integer id,
            @RequestBody ImovelModel imovel) {
        ImovelModel updatedImovel = imovelService.update(id, imovel);
        if (updatedImovel == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedImovel);
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!imovelService.delete(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}