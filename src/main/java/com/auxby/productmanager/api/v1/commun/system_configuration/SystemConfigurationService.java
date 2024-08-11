package com.auxby.productmanager.api.v1.commun.system_configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemConfigurationService {

    private static final String APP_CURRENCY_PATTERN = "APP_CURRENCY_%s";
    private static final String APP_ALLOWED_FILES = "APP_ALLOWED_FILES";
    private static final int DEFAULT_ALLOWED_FILES = 8;

    private final SystemConfigurationRepository repository;

    @Cacheable(cacheNames = "longLivedCache", key = "{#currency}")
    public SystemConfiguration getSystemCurrency(String currency) {
        var key = currency.equalsIgnoreCase("EURO") ? "EUR" : currency;
        return repository.findByCodePattern(String.format(APP_CURRENCY_PATTERN, key))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("System configured currency not found for :" + currency));
    }

    @Cacheable(cacheNames = "longLivedCache")
    public int getAllowedFilesNumber() {
        var value = repository.findByCodePattern(APP_ALLOWED_FILES)
                .stream()
                .findFirst();
        return value.map(systemConfiguration -> Integer.parseInt(systemConfiguration.getValue()))
                .orElse(DEFAULT_ALLOWED_FILES);
    }
}
