package com.java.test.junior;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseToxiproxyIT {

    private static final Network network = Network.newNetwork();

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine").withNetwork(network).withNetworkAliases("postgres");

    @Container
    static final ToxiproxyContainer toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0").withNetwork(network);

    protected static ToxiproxyContainer.ContainerProxy dbProxy;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        toxiproxy.start();

        if (dbProxy == null) {
            dbProxy = toxiproxy.getProxy(postgres, 5432);
        }

        String proxyHost = dbProxy.getContainerIpAddress();
        int proxyMappedPort = dbProxy.getProxyPort();

        registry.add("spring.datasource.url", () -> String.format("jdbc:postgresql://%s:%d/%s?socketTimeout=3", proxyHost, proxyMappedPort, postgres.getDatabaseName()));

        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    protected void cutConnection() {
        if (dbProxy != null) {
            dbProxy.setConnectionCut(true);
        }
    }

    protected void restoreConnection() {
        if (dbProxy != null) {
            dbProxy.setConnectionCut(false);
        }
    }
}