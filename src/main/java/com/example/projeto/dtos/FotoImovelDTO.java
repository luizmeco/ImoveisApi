package com.example.projeto.dtos;

import com.example.projeto.models.FotoImovelModel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FotoImovelDTO {
    private Integer id;
    private String nomeArquivo;
    private Boolean capa;
    private Integer ordem;
    private Integer imovelId;
    private String urlArquivo;

    public FotoImovelDTO(FotoImovelModel foto) {
        this.id = foto.getId();
        this.nomeArquivo = foto.getNomeArquivo();
        this.capa = foto.getCapa();
        this.ordem = foto.getOrdem();
        this.imovelId = foto.getImovel().getId();
        this.urlArquivo = "/fotos/" + foto.getId() + "/arquivo";
    }
}