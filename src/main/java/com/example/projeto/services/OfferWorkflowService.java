package com.example.projeto.services;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.projeto.models.ContractModel;
import com.example.projeto.models.ImovelModel;
import com.example.projeto.models.OfferModel;
import com.example.projeto.models.UserModel;
import com.example.projeto.repositories.ContractRepository;
import com.example.projeto.repositories.ImovelRepository;
import com.example.projeto.repositories.OfferRepository;
import com.example.projeto.repositories.UserRepository;

@Service
public class OfferWorkflowService {
    private static final System.Logger LOGGER = System.getLogger(OfferWorkflowService.class.getName());

    private final OfferRepository offerRepository;
    private final ContractRepository contractRepository;
    private final ImovelRepository imovelRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public OfferWorkflowService(
            OfferRepository offerRepository,
            ContractRepository contractRepository,
            ImovelRepository imovelRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.offerRepository = offerRepository;
        this.contractRepository = contractRepository;
        this.imovelRepository = imovelRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public OfferModel createOffer(String email, Integer propertyId, String type, BigDecimal value, String message) {
        ImovelModel property = imovelRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));
        UserModel user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (!isAvailable(property.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este imóvel não está disponível para ofertas.");
        }
        String normalizedType = normalize(type);
        if (!normalizedType.equals("ALUGUEL") && !normalizedType.equals("VENDA")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo da oferta deve ser aluguel ou venda.");
        }
        if (!normalize(property.getFinalidade()).contains(normalizedType)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A finalidade da oferta não corresponde ao imóvel.");
        }
        if (value == null || value.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor proposto deve ser maior que zero.");
        }
        if (offerRepository.existsByImovel_IdAndUsuario_IdAndStatusIgnoreCase(propertyId, user.getId(), "PENDENTE")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já tem uma oferta pendente para este imóvel.");
        }

        OfferModel offer = new OfferModel();
        offer.setImovel(property);
        offer.setUsuario(user);
        offer.setTipoOferta(normalizedType);
        offer.setValorProposto(value);
        offer.setMensagem(message == null ? "" : message.trim());
        return offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public List<OfferModel> listOffers(String email, boolean staff) {
        return staff ? offerRepository.findAllByOrderByCreatedAtDesc()
                : offerRepository.findAllByUsuario_EmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    @Transactional
    public OfferModel updateOfferStatus(Integer offerId, String status) {
        OfferModel offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta não encontrada."));
        if (!"PENDENTE".equalsIgnoreCase(offer.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A oferta não está mais pendente.");
        }

        String normalizedStatus = normalize(status);
        if (normalizedStatus.equals("RECUSADA")) {
            offer.setStatus("RECUSADA");
            return offerRepository.save(offer);
        }
        if (!normalizedStatus.equals("ACEITA")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status deve ser ACEITA ou RECUSADA.");
        }
        if (!isAvailable(offer.getImovel().getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O imóvel não está mais disponível.");
        }

        offer.setStatus("ACEITA");
        offer.getImovel().setStatus("RESERVADO");
        imovelRepository.save(offer.getImovel());
        offerRepository.findAllByImovel_IdAndStatusIgnoreCase(offer.getImovel().getId(), "PENDENTE")
                .stream()
                .filter(other -> !other.getId().equals(offerId))
                .forEach(other -> other.setStatus("EM_ESPERA"));
        offerRepository.saveAll(offerRepository.findAllByImovel_IdAndStatusIgnoreCase(offer.getImovel().getId(), "EM_ESPERA"));

        ContractModel contract = new ContractModel();
        contract.setOferta(offer);
        contractRepository.save(contract);
        return offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public List<ContractModel> listContracts(String email, boolean staff) {
        return staff ? contractRepository.findAllByOrderByDataCriacaoDesc()
                : contractRepository.findAllByOferta_Usuario_EmailIgnoreCaseOrderByDataCriacaoDesc(email);
    }

    @Transactional
    public ContractModel approveContract(Integer contractId) {
        ContractModel contract = findContract(contractId);
        if (!"AGUARDANDO_APROVACAO".equalsIgnoreCase(contract.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O contrato não está aguardando aprovação.");
        }
        contract.setStatus("AGUARDANDO_ASSINATURA");
        contract.setDataAprovacao(LocalDateTime.now());
        ContractModel approved = contractRepository.save(contract);
        notifyClient(approved, "Contrato aprovado", "Seu contrato foi aprovado e já pode ser assinado na sua área do cliente.");
        return approved;
    }

    @Transactional
    public ContractModel refuseContract(Integer contractId) {
        ContractModel contract = findContract(contractId);
        if (!"AGUARDANDO_APROVACAO".equalsIgnoreCase(contract.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O contrato não está aguardando aprovação.");
        }
        contract.setStatus("RECUSADO");
        contract.getOferta().setStatus("RECUSADA");
        releaseProperty(contract.getOferta().getImovel());
        offerRepository.save(contract.getOferta());
        ContractModel refused = contractRepository.save(contract);
        notifyClient(refused, "Contrato recusado", "Seu contrato foi recusado. Consulte sua área do cliente para mais informações.");
        return refused;
    }

    @Transactional
    public ContractModel cancelContract(Integer contractId) {
        ContractModel contract = findContract(contractId);
        if ("CANCELADO".equalsIgnoreCase(contract.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O contrato já está cancelado.");
        }
        contract.setStatus("CANCELADO");
        contract.getOferta().setStatus("CANCELADA");
        releaseProperty(contract.getOferta().getImovel());
        offerRepository.save(contract.getOferta());
        ContractModel cancelled = contractRepository.save(contract);
        notifyClient(cancelled, "Contrato cancelado", "O contrato foi cancelado e o imóvel voltou a ficar disponível.");
        return cancelled;
    }

    @Transactional
    public ContractModel signContract(Integer contractId, String email) {
        ContractModel contract = findContract(contractId);
        if (!contract.getOferta().getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este contrato pertence a outro cliente.");
        }
        if (!"AGUARDANDO_ASSINATURA".equalsIgnoreCase(contract.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O contrato ainda não está liberado para assinatura.");
        }

        contract.setStatus("ASSINADO");
        contract.setDataAssinatura(LocalDateTime.now());
        ImovelModel property = contract.getOferta().getImovel();
        property.setStatus("ALUGUEL".equals(contract.getOferta().getTipoOferta()) ? "ALUGADO" : "VENDIDO");
        imovelRepository.save(property);
        List<OfferModel> remainingOffers = offerRepository.findAllByImovel_IdAndStatusIgnoreCase(property.getId(), "EM_ESPERA");
        remainingOffers.forEach(other -> other.setStatus("RECUSADA"));
        offerRepository.saveAll(remainingOffers);
        return contractRepository.save(contract);
    }

    private ContractModel findContract(Integer id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado."));
    }

    private void releaseProperty(ImovelModel property) {
        property.setStatus("DISPONIVEL");
        imovelRepository.save(property);
        List<OfferModel> waitingOffers = offerRepository.findAllByImovel_IdAndStatusIgnoreCase(property.getId(), "EM_ESPERA");
        waitingOffers.forEach(offer -> offer.setStatus("PENDENTE"));
        offerRepository.saveAll(waitingOffers);
    }

    private void notifyClient(ContractModel contract, String subject, String message) {
        try {
            emailService.enviarEmailTexto(contract.getOferta().getUsuario().getEmail(), subject, message);
        } catch (RuntimeException exception) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Não foi possível enviar a notificação do contrato {0}: {1}",
                    contract.getId(), exception.getMessage());
        }
    }

    private boolean isAvailable(String status) {
        return normalize(status).equals("DISPONIVEL");
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }
}