package com.example.projeto.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitacaoImovelDTO {
    private String nome;
    private String telefone;
    private String email;
    private String mensagem;
}