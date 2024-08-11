package com.auxby.productmanager.integration;

import com.auxby.productmanager.config.security.JwtService;
import com.auxby.productmanager.utils.service.AmazonClientService;
import com.auxby.productmanager.utils.service.BranchIOService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@Testcontainers
public abstract class TestContainerBase {

    @MockBean
    JwtService jwtService;
    @MockBean
    AmazonClientService clientService;
    @MockBean
    BranchIOService branchIOService;

    @Container
    public static MariaDBContainer<?> database = new MariaDBContainer<>("mariadb:latest")
            .withUsername("auxby-test")
            .withPassword("auxby-test")
            .withDatabaseName("auxby-test-db")
            .withInitScript("db/init.sql");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        log.info("Database URL: {} - password {} - user {}", database.getJdbcUrl(), database.getPassword(), database.getUsername());
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        database.start();
    }

    @AfterAll
    static void afterAll() {
        database.stop();
    }

    @BeforeEach
    @SneakyThrows
    void setup() {
        when(clientService.uploadPhoto(any(), anyString(), anyInt())).thenReturn("http://localhost:8080/pathToS3");
        when(clientService.convertToFile(any())).thenReturn(Mockito.mock(File.class));
        when(branchIOService.createDeepLink(anyInt(), anyString(), anyString())).thenReturn("http://localhost:8080/deepLink");
    }
}
