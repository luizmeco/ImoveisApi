package com.example.projeto.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.projeto.models.FotoImovelModel;
import com.example.projeto.models.ImovelModel;
import com.example.projeto.repositories.FotoImovelRepository;
import com.example.projeto.repositories.ImovelRepository;

@Service 
public class FotoImovelService {
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final FotoImovelRepository fotoImovelRepository;
    private final ImovelRepository imovelRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public FotoImovelService(FotoImovelRepository fotoImovelRepository, ImovelRepository imovelRepository) {
        this.fotoImovelRepository = fotoImovelRepository;
        this.imovelRepository = imovelRepository;
    }

    @Transactional(readOnly = true)
    public List<FotoImovelModel> listar(Integer imovelId) {
        if (imovelId != null) {
            return fotoImovelRepository.findByImovel_IdOrderByOrdemAscIdAsc(imovelId);
        }
        return fotoImovelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public FotoImovelModel buscar(Integer id) {
        return fotoImovelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada"));
    }

    @Transactional
    public FotoImovelModel salvarFoto(MultipartFile arquivo, Integer imovelId, Integer ordem) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma imagem não vazia");
        }
        if (ordem != null && ordem < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ordem não pode ser negativa");
        }

        ImovelModel imovel = imovelRepository.findById(imovelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado"));
        Path destino = armazenarArquivo(arquivo, imovelId);

        try {
            FotoImovelModel model = new FotoImovelModel();
            model.setCaminho(destino.toString());
            model.setNomeArquivo(destino.getFileName().toString());
            model.setCapa(false);
            model.setOrdem(ordem);
            model.setImovel(imovel);
            return fotoImovelRepository.save(model);
        } catch (RuntimeException e) {
            excluirArquivoSilenciosamente(destino);
            throw e;
        }
    }

    @Transactional
    public FotoImovelModel atualizar(Integer id, MultipartFile arquivo, Boolean capa, Integer ordem) {
        FotoImovelModel foto = buscar(id);
        Path arquivoAntigo = caminhoSeguro(foto.getCaminho());
        Path arquivoNovo = null;

        if (arquivo != null && !arquivo.isEmpty()) {
            arquivoNovo = armazenarArquivo(arquivo, foto.getImovel().getId());
            foto.setCaminho(arquivoNovo.toString());
            foto.setNomeArquivo(arquivoNovo.getFileName().toString());
        }
        if (ordem != null) {
            if (ordem < 0) {
                excluirArquivoSilenciosamente(arquivoNovo);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ordem não pode ser negativa");
            }
            foto.setOrdem(ordem);
        }
        if (Boolean.TRUE.equals(capa)) {
            definirCapaExclusiva(foto);
        } else if (Boolean.FALSE.equals(capa)) {
            foto.setCapa(false);
        }

        try {
            FotoImovelModel salva = fotoImovelRepository.save(foto);
            if (arquivoNovo != null && arquivoAntigo != null && !arquivoNovo.equals(arquivoAntigo)) {
                excluirArquivoSilenciosamente(arquivoAntigo);
            }
            return salva;
        } catch (RuntimeException e) {
            excluirArquivoSilenciosamente(arquivoNovo);
            throw e;
        }
    }

    @Transactional
    public FotoImovelModel tornarCapa(Integer id) {
        FotoImovelModel foto = buscar(id);
        definirCapaExclusiva(foto);
        return fotoImovelRepository.save(foto);
    }

    @Transactional
    public void excluir(Integer id) {
        FotoImovelModel foto = buscar(id);
        Path caminho = caminhoSeguro(foto.getCaminho());
        if (caminho != null) {
            try {
                Files.deleteIfExists(caminho);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível excluir o arquivo", e);
            }
        }
        fotoImovelRepository.delete(foto);
    }

    @Transactional(readOnly = true)
    public Path obterArquivo(Integer id) {
        FotoImovelModel foto = buscar(id);
        Path caminho = caminhoSeguro(foto.getCaminho());
        if (caminho == null || !Files.isRegularFile(caminho)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo da foto não encontrado");
        }
        return caminho;
    }

    private void definirCapaExclusiva(FotoImovelModel fotoDestaque) {
        List<FotoImovelModel> fotos = fotoImovelRepository
                .findByImovel_IdOrderByOrdemAscIdAsc(fotoDestaque.getImovel().getId());
        for (FotoImovelModel foto : fotos) {
            foto.setCapa(foto.getId().equals(fotoDestaque.getId()));
        }
        fotoImovelRepository.saveAll(fotos);
        fotoDestaque.setCapa(true);
    }

    private Path armazenarArquivo(MultipartFile arquivo, Integer imovelId) {
        String nomeOriginal = StringUtils.cleanPath(arquivo.getOriginalFilename() == null
                ? ""
                : arquivo.getOriginalFilename());
        String nomeSeguro = StringUtils.getFilename(nomeOriginal);
        int ponto = nomeSeguro == null ? -1 : nomeSeguro.lastIndexOf('.');
        String extensao = ponto < 0 ? "" : nomeSeguro.substring(ponto + 1).toLowerCase(Locale.ROOT);
        String contentType = arquivo.getContentType();

        if (!EXTENSOES_PERMITIDAS.contains(extensao)
                || (contentType != null && !contentType.toLowerCase(Locale.ROOT).startsWith("image/"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagem não permitido");
        }

        Path destino = diretorioImovel(imovelId).resolve(UUID.randomUUID() + "." + extensao).normalize();
        try {
            Files.createDirectories(destino.getParent());
            arquivo.transferTo(destino);
            return destino;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar a imagem", e);
        }
    }

    private Path diretorioImovel(Integer imovelId) {
        Path raiz = Path.of(uploadDir).toAbsolutePath().normalize();
        Path diretorio = raiz.resolve(String.valueOf(imovelId)).normalize();
        if (!diretorio.startsWith(raiz)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de imóvel inválido");
        }
        return diretorio;
    }

    private Path caminhoSeguro(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return null;
        }
        Path raiz = Path.of(uploadDir).toAbsolutePath().normalize();
        Path destino = Path.of(caminho).toAbsolutePath().normalize();
        return destino.startsWith(raiz) ? destino : null;
    }

    private void excluirArquivoSilenciosamente(Path caminho) {
        if (caminho == null) {
            return;
        }
        try {
            Files.deleteIfExists(caminho);
        } catch (IOException ignored) {
        }
    }
}
