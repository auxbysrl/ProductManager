package com.auxby.productmanager.api.v1.commun.system_configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigurationServiceTest {
    @InjectMocks
    private SystemConfigurationService service;
    @Mock
    private SystemConfigurationRepository repository;

    @Test
    void should_ReturnDefaultValueOfAllowedFiles_When_ValueNotSetInDB() {
        //Act
        var result = service.getAllowedFilesNumber();

        //Assert
        ArgumentCaptor<String> codeArg = ArgumentCaptor.forClass(String.class);
        assertAll(
                () -> assertEquals(8, result),
                () -> verify(repository).findByCodePattern(codeArg.capture()),
                () -> assertEquals("APP_ALLOWED_FILES", codeArg.getValue())
        );
    }

    @Test
    void should_ReturnValueOfAllowedFiles_When_ValueSetDB() {
        //Arrange
        var systemConfiguration = SystemConfiguration.builder()
                .value("16")
                .build();
        when(repository.findByCodePattern("APP_ALLOWED_FILES")).thenReturn(List.of(systemConfiguration));

        //Act
        var result = service.getAllowedFilesNumber();

        //Assert
        ArgumentCaptor<String> codeArg = ArgumentCaptor.forClass(String.class);
        assertAll(
                () -> assertEquals(16, result),
                () -> verify(repository).findByCodePattern(codeArg.capture()),
                () -> assertEquals("APP_ALLOWED_FILES", codeArg.getValue())
        );
    }

    @Test
    void should_ReturnAllConfiguredCurrenciesForEuro_When_PresentInDB() {
        //Arrange
        var systemConfiguration = SystemConfiguration.builder()
                .value("1")
                .build();
        when(repository.findByCodePattern("APP_CURRENCY_EUR")).thenReturn(List.of(systemConfiguration));

        //Act
        var result = service.getSystemCurrency("EURO");

        //Assert
        ArgumentCaptor<String> codeArg = ArgumentCaptor.forClass(String.class);
        assertAll(
                () -> assertEquals("1", result.getValue()),
                () -> verify(repository).findByCodePattern(codeArg.capture()),
                () -> assertEquals("APP_CURRENCY_EUR", codeArg.getValue())
        );
    }
}