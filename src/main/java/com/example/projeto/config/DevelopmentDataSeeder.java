package com.example.projeto.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.projeto.models.BairroModel;
import com.example.projeto.models.ImovelModel;
import com.example.projeto.models.TipoImovelModel;
import com.example.projeto.repositories.BairroRepository;
import com.example.projeto.repositories.ImovelRepository;
import com.example.projeto.repositories.TipoImovelRepository;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DevelopmentDataSeeder implements CommandLineRunner {

    private final BairroRepository bairroRepository;
    private final TipoImovelRepository tipoImovelRepository;
    private final ImovelRepository imovelRepository;

    public DevelopmentDataSeeder(
            BairroRepository bairroRepository,
            TipoImovelRepository tipoImovelRepository,
            ImovelRepository imovelRepository) {
        this.bairroRepository = bairroRepository;
        this.tipoImovelRepository = tipoImovelRepository;
        this.imovelRepository = imovelRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        BairroModel cambui = findOrCreateBairro(
                "Cambuí", "Campinas", "13024-000", "13025-999");
        BairroModel taquaral = findOrCreateBairro(
                "Taquaral", "Campinas", "13076-000", "13087-999");
        BairroModel baraoGeraldo = findOrCreateBairro(
                "Barão Geraldo", "Campinas", "13082-000", "13085-999");

        TipoImovelModel casa = findOrCreateTipo(
                "Casa", "Casa residencial fictícia para demonstração.");
        TipoImovelModel apartamento = findOrCreateTipo(
                "Apartamento", "Apartamento fictício para demonstração.");
        TipoImovelModel sobrado = findOrCreateTipo(
                "Sobrado", "Sobrado fictício em bairro residencial.");
        TipoImovelModel cobertura = findOrCreateTipo(
                "Cobertura", "Cobertura fictícia com área de lazer.");

        List<ImovelModel> imoveis = List.of(
                imovel("Apartamento ensolarado no Cambuí",
                        "Imóvel fictício, amplo e próximo a cafés e serviços.",
                        "Venda", 685000, null, 2, 2, 1, 86, 78,
                        "Rua dos Alecrins", "120", "13024-410",
                        "Varanda, armários planejados e uma vaga coberta.",
                        true, apartamento, cambui),
                imovel("Casa com quintal no Taquaral",
                        "Casa fictícia com área externa e ambientes integrados.",
                        "Venda", 920000, null, 3, 3, 2, 240, 168,
                        "Rua das Paineiras", "45", "13087-120",
                        "Quintal arborizado, churrasqueira e suíte.",
                        true, casa, taquaral),
                imovel("Studio mobiliado no Cambuí",
                        "Studio fictício mobiliado, perto do centro e de serviços.",
                        "Aluguel", null, 2800, 1, 1, 1, 42, 38,
                        "Rua Coronel Quirino", "780", "13025-002",
                        "Mobiliado, portaria e lavanderia compartilhada.",
                        false, apartamento, cambui),
                imovel("Sobrado familiar em Barão Geraldo",
                        "Sobrado fictício em rua tranquila, perto da universidade.",
                        "Venda", 780000, null, 3, 3, 2, 210, 155,
                        "Rua do Sol", "215", "13084-010",
                        "Escritório, varanda e espaço gourmet.",
                        true, sobrado, baraoGeraldo),
                imovel("Apartamento compacto no Taquaral",
                        "Apartamento fictício em condomínio com área de lazer.",
                        "Aluguel", null, 2350, 2, 1, 1, 58, 52,
                        "Avenida Nossa Senhora de Fátima", "990", "13090-903",
                        "Piscina, academia e salão de festas.",
                        false, apartamento, taquaral),
                imovel("Cobertura com terraço no Cambuí",
                        "Cobertura fictícia com vista aberta e área de convivência.",
                        "Venda", 1450000, null, 3, 4, 2, 190, 160,
                        "Rua Barreto Leme", "1550", "13025-085",
                        "Terraço privativo, churrasqueira e suíte master.",
                        true, cobertura, cambui),
                imovel("Casa térrea em Barão Geraldo",
                        "Casa fictícia térrea com jardim em rua residencial.",
                        "Aluguel", null, 3900, 3, 2, 2, 250, 140,
                        "Rua Maria Tereza Dias da Silva", "62", "13083-896",
                        "Jardim, edícula e garagem coberta.",
                        false, casa, baraoGeraldo),
                imovel("Apartamento novo no Taquaral",
                        "Apartamento fictício de dois dormitórios em condomínio moderno.",
                        "Venda", 545000, null, 2, 2, 1, 74, 68,
                        "Rua Paula Bueno", "1340", "13076-061",
                        "Sacada, elevador e preparação para ar-condicionado.",
                        false, apartamento, taquaral));

        imoveis.stream()
                .filter(property -> !imovelRepository.existsByTituloIgnoreCase(property.getTitulo()))
                .forEach(imovelRepository::save);
    }

    private BairroModel findOrCreateBairro(
            String nome, String cidade, String cepInicial, String cepFinal) {
        return bairroRepository.findFirstByNomeIgnoreCaseAndCidadeIgnoreCase(nome, cidade)
                .orElseGet(() -> {
                    BairroModel bairro = new BairroModel();
                    bairro.setNome(nome);
                    bairro.setCidade(cidade);
                    bairro.setEstado("SP");
                    bairro.setCepInicial(cepInicial);
                    bairro.setCepFinal(cepFinal);
                    return bairroRepository.save(bairro);
                });
    }

    private TipoImovelModel findOrCreateTipo(String nome, String descricao) {
        return tipoImovelRepository.findFirstByNomeIgnoreCase(nome)
                .orElseGet(() -> {
                    TipoImovelModel tipo = new TipoImovelModel();
                    tipo.setNome(nome);
                    tipo.setDescricao(descricao);
                    return tipoImovelRepository.save(tipo);
                });
    }

    private ImovelModel imovel(
            String titulo,
            String descricao,
            String finalidade,
            Integer precoVenda,
            Integer precoAluguel,
            int dormitorios,
            int banheiros,
            int garagem,
            int areaTotal,
            int areaConstruida,
            String endereco,
            String numero,
            String cep,
            String caracteristicas,
            boolean destaque,
            TipoImovelModel tipo,
            BairroModel bairro) {
        ImovelModel imovel = new ImovelModel();
        imovel.setTitulo(titulo);
        imovel.setDescricao(descricao);
        imovel.setFinalidade(finalidade);
        imovel.setStatus("Disponível");
        imovel.setPrecoVenda(decimal(precoVenda));
        imovel.setPrecoAluguel(decimal(precoAluguel));
        imovel.setDormitorios(dormitorios);
        imovel.setBanheiros(banheiros);
        imovel.setGaragem(garagem);
        imovel.setAreaTotal(BigDecimal.valueOf(areaTotal));
        imovel.setAreaConstruida(BigDecimal.valueOf(areaConstruida));
        imovel.setEndereco(endereco);
        imovel.setNumero(numero);
        imovel.setCep(cep);
        imovel.setCaracteristicas(caracteristicas);
        imovel.setDestaque(destaque);
        imovel.setTipoImovel(tipo);
        imovel.setBairro(bairro);
        return imovel;
    }

    private BigDecimal decimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
