package com.example.projeto.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.projeto.models.ContractModel;
import com.example.projeto.models.ImovelModel;
import com.example.projeto.models.OfferModel;
import com.example.projeto.models.UserModel;
import com.example.projeto.repositories.ContractRepository;
import com.example.projeto.repositories.ImovelRepository;
import com.example.projeto.repositories.OfferRepository;
import com.example.projeto.repositories.UserRepository;

class OfferWorkflowServiceTests {
    private final OfferRepository offerRepository = org.mockito.Mockito.mock(OfferRepository.class);
    private final ContractRepository contractRepository = org.mockito.Mockito.mock(ContractRepository.class);
    private final ImovelRepository imovelRepository = org.mockito.Mockito.mock(ImovelRepository.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final EmailService emailService = org.mockito.Mockito.mock(EmailService.class);
    private OfferWorkflowService service;

    @BeforeEach
    void setUp() {
        service = new OfferWorkflowService(offerRepository, contractRepository, imovelRepository, userRepository, emailService);
    }

    @Test
    void rejectsSecondPendingOfferForSameClientAndProperty() {
        ImovelModel property = property(10, "DISPONIVEL", "VENDA");
        UserModel user = new UserModel(7, "Cliente", "cliente@example.com", "", "USER");
        when(imovelRepository.findById(10)).thenReturn(Optional.of(property));
        when(userRepository.findByEmailIgnoreCase("cliente@example.com")).thenReturn(Optional.of(user));
        when(offerRepository.existsByImovel_IdAndUsuario_IdAndStatusIgnoreCase(10, 7, "PENDENTE")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.createOffer("cliente@example.com", 10, "VENDA", BigDecimal.valueOf(500000), ""));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void acceptingOfferReservesPropertyAndCreatesContract() {
        ImovelModel property = property(10, "DISPONIVEL", "VENDA");
        UserModel user = new UserModel(7, "Cliente", "cliente@example.com", "", "USER");
        OfferModel accepted = offer(1, property, user, "PENDENTE");
        OfferModel waiting = offer(2, property, new UserModel(8, "Outro", "outro@example.com", "", "USER"), "PENDENTE");
        when(offerRepository.findById(1)).thenReturn(Optional.of(accepted));
        when(offerRepository.findAllByImovel_IdAndStatusIgnoreCase(10, "PENDENTE")).thenReturn(List.of(waiting));
        when(offerRepository.findAllByImovel_IdAndStatusIgnoreCase(10, "EM_ESPERA")).thenReturn(List.of(waiting));
        when(offerRepository.save(any(OfferModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contractRepository.save(any(ContractModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OfferModel result = service.updateOfferStatus(1, "ACEITA");

        assertEquals("ACEITA", result.getStatus());
        assertEquals("RESERVADO", property.getStatus());
        assertEquals("EM_ESPERA", waiting.getStatus());
        verify(offerRepository).saveAll(List.of(waiting));
        verify(contractRepository).save(org.mockito.ArgumentMatchers.argThat(contract ->
                contract.getOferta() == accepted && contract.getStatus().equals("AGUARDANDO_APROVACAO")));
    }

    private ImovelModel property(Integer id, String status, String purpose) {
        ImovelModel property = new ImovelModel();
        property.setId(id);
        property.setStatus(status);
        property.setFinalidade(purpose);
        return property;
    }

    private OfferModel offer(Integer id, ImovelModel property, UserModel user, String status) {
        OfferModel offer = new OfferModel();
        offer.setId(id);
        offer.setImovel(property);
        offer.setUsuario(user);
        offer.setTipoOferta("VENDA");
        offer.setValorProposto(BigDecimal.valueOf(500000));
        offer.setStatus(status);
        return offer;
    }
}