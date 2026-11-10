package com.example.projeto.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.projeto.models.BairroModel;
import com.example.projeto.models.ImovelModel;
import com.example.projeto.models.TipoImovelModel;
import com.example.projeto.repositories.BairroRepository;
import com.example.projeto.repositories.ImovelRepository;
import com.example.projeto.repositories.TipoImovelRepository;

class DevelopmentDataSeederTests {

    private final BairroRepository bairroRepository = org.mockito.Mockito.mock(BairroRepository.class);
    private final TipoImovelRepository tipoImovelRepository = org.mockito.Mockito.mock(TipoImovelRepository.class);
    private final ImovelRepository imovelRepository = org.mockito.Mockito.mock(ImovelRepository.class);
    private final DevelopmentDataSeeder seeder =
            new DevelopmentDataSeeder(bairroRepository, tipoImovelRepository, imovelRepository);

    @BeforeEach
    void returnSavedEntitiesFromRepositories() {
        when(bairroRepository.findFirstByNomeIgnoreCaseAndCidadeIgnoreCase(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());
        when(tipoImovelRepository.findFirstByNomeIgnoreCase(
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());
        when(bairroRepository.save(any(BairroModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tipoImovelRepository.save(any(TipoImovelModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(imovelRepository.save(any(ImovelModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsSampleNeighborhoodsTypesAndProperties() {
        seeder.run();

        verify(bairroRepository, times(3)).save(any(BairroModel.class));
        verify(tipoImovelRepository, times(4)).save(any(TipoImovelModel.class));
        verify(imovelRepository, times(8)).save(any(ImovelModel.class));
    }

    @Test
    void doesNotDuplicateExistingSeedRecords() {
        when(bairroRepository.findFirstByNomeIgnoreCaseAndCidadeIgnoreCase(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(new BairroModel()));
        when(tipoImovelRepository.findFirstByNomeIgnoreCase(
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(new TipoImovelModel()));
        when(imovelRepository.existsByTituloIgnoreCase(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);

        seeder.run();

        verify(bairroRepository, never()).save(any(BairroModel.class));
        verify(tipoImovelRepository, never()).save(any(TipoImovelModel.class));
        verify(imovelRepository, never()).save(any(ImovelModel.class));
    }
}
