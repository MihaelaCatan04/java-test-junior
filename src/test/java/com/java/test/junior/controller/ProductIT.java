package com.java.test.junior.controller;

import com.java.test.junior.BaseIT;
import com.java.test.junior.model.ErrorResponse;
import com.java.test.junior.model.PageResponse;
import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.model.ProductResponseDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductIT extends BaseIT {

    private static final String ALICE = "alice";
    private static final String ALICE_PASS = "alice123";
    private static final String JOHNY = "johny";
    private static final String JOHNY_PASS = "johny123";

    private static final String PRODUCTS = "/products";
    private static final String PRODUCTS_BY_ID = "/products/%d";
    private static final String PRODUCTS_PAGINATION = "/products?page=%d&page_size=%d";
    private static final String PRODUCTS_BY_NAME = "/products/name/%s";

    private static Long aliceProductId;
    private static Long johnyProductId;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void aliceCreatesProduct_returnsCreatedWithId() {
        ResponseEntity<ProductResponseDTO> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .postForEntity(PRODUCTS,
                                new ProductDTO("Alice's Gadget", 49.99, "A neat gadget"),
                                ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ProductResponseDTO body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());
        assertEquals("Alice's Gadget", body.getName());
        assertEquals(ALICE, body.getUsername());
        aliceProductId = body.getId();
    }

    @Test
    @Order(2)
    void johnyCreatesProduct_returnsCreated() {
        ResponseEntity<ProductResponseDTO> response =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .postForEntity(PRODUCTS,
                                new ProductDTO("Johny's Widget", 19.99, "A small widget"),
                                ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());
        johnyProductId = response.getBody().getId();
    }

    @Test
    @Order(3)
    void unauthenticatedCreate_returns401() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity(PRODUCTS,
                        new ProductDTO("Anonymous Product", 1.0, "Desc"),
                        ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void getProductById_returnsProduct() {
        ResponseEntity<ProductResponseDTO> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_BY_ID, aliceProductId),
                                ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertNotNull(response.getBody());
        assertEquals(aliceProductId, response.getBody().getId());
    }

    @Test
    @Order(5)
    void getProductById_nonExistent_returns404() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_BY_ID, 999999L),
                                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(6)
    void getProductById_invalidId_returns400() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_BY_ID, -1L),
                                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(7)
    void getProductsByName_returnsMatchingProducts() {
        ResponseEntity<List<ProductResponseDTO>> response =
                restTemplate.exchange(
                        String.format(PRODUCTS_BY_NAME, "Alice"),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    @Order(8)
    void getProductsByName_noMatch_returnsEmptyList() {
        ResponseEntity<List<ProductResponseDTO>> response =
                restTemplate.exchange(
                        String.format(PRODUCTS_BY_NAME, "NonExistent"),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @Order(9)
    void ownerCanModifyProduct() {
        HttpEntity<ProductDTO> request = new HttpEntity<>(
                new ProductDTO("Updated Gadget", 59.99, "Updated description"));

        ResponseEntity<ProductResponseDTO> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, aliceProductId),
                                HttpMethod.PUT, request, ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals("Updated Gadget", response.getBody().getName());
        assertEquals(59.99, response.getBody().getPrice());
    }

    @Test
    @Order(10)
    void nonOwnerCannotModifyProduct_returns403() {
        HttpEntity<ProductDTO> request = new HttpEntity<>(
                new ProductDTO("New Name", 0.0, "New desc"));

        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, aliceProductId),
                                HttpMethod.PUT, request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(11)
    void modifyNonExistentProduct_returns404() {
        HttpEntity<ProductDTO> request = new HttpEntity<>(
                new ProductDTO("Ghost", 1.0, "Ghost desc"));

        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, 999999L),
                                HttpMethod.PUT, request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(12)
    void nonOwnerCannotDeleteProduct_returns403() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, aliceProductId),
                                HttpMethod.DELETE, null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(13)
    void ownerCanDeleteOwnProduct() {
        ResponseEntity<Void> response =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, johnyProductId),
                                HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(14)
    void deletedProduct_returns404OnGet() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_BY_ID, johnyProductId),
                                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(15)
    void deleteNonExistentProduct_returns404() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, 999999L),
                                HttpMethod.DELETE, null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(16)
    void pagination_returnsCorrectPageSize() {
        for (int i = 0; i < 5; i++) {
            restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                    .postForEntity(PRODUCTS,
                            new ProductDTO("Bulk Product " + i, 10.0 + i, "Bulk desc"),
                            ProductResponseDTO.class);
        }

        ResponseEntity<PageResponse<ProductResponseDTO>> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_PAGINATION, 1, 2),
                                HttpMethod.GET, null,
                                new ParameterizedTypeReference<>() {
                                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<ProductResponseDTO> page = response.getBody();
        assertNotNull(page);
        assertThat(page.getContent().size()).isEqualTo(2);
        assertThat(page.getCurrentPage()).isEqualTo(1);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(6);
    }

    @Test
    @Order(17)
    void pagination_pageExceedsTotal_returns400OrNotFound() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_PAGINATION, 9999, 2),
                                HttpMethod.GET, null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND);
    }

}