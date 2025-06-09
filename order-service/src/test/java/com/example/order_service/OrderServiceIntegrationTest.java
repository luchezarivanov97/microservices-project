package com.example.order_service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderServiceIntegrationTest {

    static Network network = Network.newNetwork();

    static PostgreSQLContainer<?> orderDb = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("order_db")
            .withUsername("postgres")
            .withPassword("newpassword")
            .withNetwork(network)
            .withNetworkAliases("order-postgres");

    static PostgreSQLContainer<?> userDb = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("userdb")
            .withUsername("postgres")
            .withPassword("newpassword")
            .withNetwork(network)
            .withNetworkAliases("user-postgres");

    static PostgreSQLContainer<?> productDb = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("product_db")
            .withUsername("postgres")
            .withPassword("newpassword")
            .withNetwork(network)
            .withNetworkAliases("product-postgres");

    static GenericContainer<?> userService = new GenericContainer<>("user-service:latest")
            .withExposedPorts(8080)
            .withNetwork(network)
            .withNetworkAliases("user-service")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://user-postgres:5432/user_db")
            .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "newpassword");

    static GenericContainer<?> productService = new GenericContainer<>("product-service:latest")
            .withExposedPorts(8081)
            .withNetwork(network)
            .withNetworkAliases("product-service")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://product-postgres:5432/product_db")
            .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "newpassword");

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeAll
    void setUp() {
        orderDb.start();
        userDb.start();
        productDb.start();
        userService.start();
        productService.start();
    }

    @AfterAll
    void tearDown() {
        productService.stop();
        userService.stop();
        productDb.stop();
        userDb.stop();
        orderDb.stop();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // Order-service DB
        registry.add("spring.datasource.url", orderDb::getJdbcUrl);
        registry.add("spring.datasource.username", orderDb::getUsername);
        registry.add("spring.datasource.password", orderDb::getPassword);

        // Service URLs - via internal Docker network
        registry.add("services.user.base-url", () -> "http://user-service:8080");
        registry.add("services.product.base-url", () -> "http://product-service:8081");
    }

    @Test
    void createOrder_withValidUserAndProduct_shouldSucceed() {
        var request = new OrderRequest(1L, 1L);
        var response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/orders",
                request,
                String.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    record OrderRequest(Long userId, Long productId) {}
}
