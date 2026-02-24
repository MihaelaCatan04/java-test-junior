package com.java.test.junior.controller;

import com.java.test.junior.BaseIT;
import com.java.test.junior.model.LoadingDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class LoadingIT extends BaseIT {

    private static final String ADMIN =
            System.getProperty("APP_ADMIN_DEFAULT_USERNAME");
    private static final String ADMIN_PASS =
            System.getProperty("APP_ADMIN_DEFAULT_PASSWORD");

    private static final String PRODUCTS_LOADING =
            "/products/loading/products";

    private static final String PRODUCTS_CSV =
            "src/test/resources/products.csv";

    private static final String PRODUCTS_URL =
            "https://raw.githubusercontent.com/MihaelaCatan04/java-test-junior/main/src/main/resources/products.csv";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void loadProductsFromURL() {
        ResponseEntity<Void> response =
                restTemplate.withBasicAuth(ADMIN, ADMIN_PASS)
                        .postForEntity(PRODUCTS_LOADING,
                                new LoadingDTO(PRODUCTS_URL),
                                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void loadProductsFromCSV() {
        ResponseEntity<Void> response =
                restTemplate.withBasicAuth(ADMIN, ADMIN_PASS)
                        .postForEntity(PRODUCTS_LOADING,
                                new LoadingDTO(PRODUCTS_CSV),
                                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

}