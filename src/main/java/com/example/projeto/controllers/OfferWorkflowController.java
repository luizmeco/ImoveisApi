package com.example.projeto.controllers;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projeto.models.ContractModel;
import com.example.projeto.models.OfferModel;
import com.example.projeto.services.OfferWorkflowService;

@RestController
public class OfferWorkflowController {
    private final OfferWorkflowService workflowService;

    public OfferWorkflowController(OfferWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/ofertas")
    public ResponseEntity<OfferModel> createOffer(@RequestBody OfferRequest request, Principal principal) {
        OfferModel created = workflowService.createOffer(
                principal.getName(), request.imovelId(), request.tipoOferta(), request.valorProposto(), request.mensagem());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/ofertas")
    public List<OfferModel> listOffers(Principal principal, Authentication authentication) {
        return workflowService.listOffers(principal.getName(), isStaff(authentication));
    }

    @PatchMapping("/ofertas/{id}/status")
    public OfferModel updateOffer(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        return workflowService.updateOfferStatus(id, request.get("status"));
    }

    @GetMapping("/contratos")
    public List<ContractModel> listContracts(Principal principal, Authentication authentication) {
        return workflowService.listContracts(principal.getName(), isStaff(authentication));
    }

    @PatchMapping("/contratos/{id}/aprovar")
    public ContractModel approveContract(@PathVariable Integer id) {
        return workflowService.approveContract(id);
    }

    @PatchMapping("/contratos/{id}/recusar")
    public ContractModel refuseContract(@PathVariable Integer id) {
        return workflowService.refuseContract(id);
    }

    @PatchMapping("/contratos/{id}/cancelar")
    public ContractModel cancelContract(@PathVariable Integer id) {
        return workflowService.cancelContract(id);
    }

    @PatchMapping("/contratos/{id}/assinar")
    public ContractModel signContract(@PathVariable Integer id, Principal principal) {
        return workflowService.signContract(id, principal.getName());
    }

    private boolean isStaff(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_CORRETOR"));
    }

    public record OfferRequest(Integer imovelId, String tipoOferta, BigDecimal valorProposto, String mensagem) {}
}