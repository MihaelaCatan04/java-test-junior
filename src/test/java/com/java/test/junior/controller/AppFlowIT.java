package com.java.test.junior.controller;

import com.java.test.junior.BaseIT;
import com.java.test.junior.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppFlowIT extends BaseIT {

    private static final String ALICE = "alice";
    private static final String ALICE_PASS = "alice123";
    private static final String JOHNY = "johny";
    private static final String JOHNY_PASS = "johny123";
    private static final String ADMIN = System.getProperty("ADMIN_USERNAME");
    private static final String ADMIN_PASS = System.getProperty("ADMIN_PASSWORD");

    private static final String AUTH_REGISTER = "/auth/register";

    private static final String PRODUCTS = "/products";
    private static final String PRODUCTS_BY_ID = "/products/%d";
    private static final String PRODUCTS_BY_NAME = "/products/name/%s";
    private static final String PRODUCTS_LIKE = "/products/%d/like";
    private static final String PRODUCTS_DISLIKE = "/products/%d/dislike";
    private static final String PRODUCTS_PAGINATION = "/products?page=%d&page_size=%d";
    private static final String PRODUCTS_LOADING = "/products/loading/products";

    private static Long productId;

    private static final String PRODUCTS_URL = "https://raw.githubusercontent.com/MihaelaCatan04/java-test-junior/main/src/main/resources/products.csv";
    private static final String PRODUCTS_CSV = "src/test/resources/products.csv";

    @Autowired
    private TestRestTemplate testRestTemplate;


    @Test
    @Order(1)
    void aliceRegistration() {
        UserRegistrationDTO request = new UserRegistrationDTO(ALICE, ALICE_PASS);

        ResponseEntity<UserResponseDTO> response =
                testRestTemplate.postForEntity(AUTH_REGISTER, request, UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getUsername()).isEqualTo(ALICE);
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    @Order(2)
    void johnyRegistration() {
        UserRegistrationDTO request = new UserRegistrationDTO(JOHNY, JOHNY_PASS);

        ResponseEntity<UserResponseDTO> response =
                testRestTemplate.postForEntity(AUTH_REGISTER, request, UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getUsername()).isEqualTo(JOHNY);
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    @Order(3)
    void rejectDuplicateRegistration() {
        UserRegistrationDTO request = new UserRegistrationDTO(ALICE, "anotherPass");

        ResponseEntity<ErrorResponse> response =
                testRestTemplate.postForEntity(AUTH_REGISTER, request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage()).isEqualTo("User already exists!");
    }

    // ================= PRODUCTS =================

    @Test
    @Order(4)
    void unauthenticatedUserCannotCreateProduct() {
        ProductDTO request = new ProductDTO("Test Product", 100.00, "Test description");

        ResponseEntity<ErrorResponse> response =
                testRestTemplate.postForEntity(PRODUCTS, request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    void aliceCreatesProduct() {
        ProductDTO request = new ProductDTO("Test Product", 100.00, "Test description");

        ResponseEntity<ProductResponseDTO> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .postForEntity(PRODUCTS, request, ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Test Product");
        assertThat(response.getBody().getPrice()).isEqualTo(100.00);
        assertThat(response.getBody().getDescription()).isEqualTo("Test description");
        assertThat(response.getBody().getUsername()).isEqualTo(ALICE);

        productId = response.getBody().getId();
    }

    @Test
    @Order(6)
    void getProductById() {
        ResponseEntity<ProductResponseDTO> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_BY_ID, productId), ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getUsername()).isEqualTo(ALICE);
    }

    @Test
    @Order(7)
    void getNonexistentProductById() {
        ResponseEntity<ErrorResponse> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_BY_ID, 100), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found");
    }

    @Test
    @Order(8)
    void getProductByNegativeId() {
        ResponseEntity<ErrorResponse> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_BY_ID, -1), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Product id must be positive");
    }

    @Test
    @Order(9)
    void getProductByName() {
        ResponseEntity<List<ProductResponseDTO>> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(
                                String.format(PRODUCTS_BY_NAME, "Test"),
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<ProductResponseDTO>>() {}
                        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @Order(10)
    void createProductsForPagination() {
        for (int i = 0; i < 4; i++) {
            ProductDTO request = new ProductDTO("Product " + i, 100.00, "Test description");

            ResponseEntity<ProductResponseDTO> response =
                    testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                            .postForEntity(PRODUCTS, request, ProductResponseDTO.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Test
    @Order(11)
    void getFirstPage() {
        ResponseEntity<PageResponse<ProductResponseDTO>> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(
                                String.format(PRODUCTS_PAGINATION, 1, 2),
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<PageResponse<ProductResponseDTO>>() {}
                        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<ProductResponseDTO> body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getContent().size()).isEqualTo(2);
        assertThat(body.getTotalElements()).isEqualTo(5L);
        assertThat(body.getTotalPages()).isEqualTo(3);
    }

    @Test
    @Order(12)
    void getInexistentPage() {
        ResponseEntity<ErrorResponse> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_PAGINATION, 100, 2), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Page 100 does not exist");
    }

    @Test
    @Order(13)
    void johnyLikesProduct() {
        ResponseEntity<Integer> response =
                testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_LIKE, productId), HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1);
    }

    @Test
    @Order(14)
    void johnyLikesAgainProduct() {
        ResponseEntity<Integer> response =
                testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_LIKE, productId), HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0);
    }

    @Test
    @Order(15)
    void johnyDislikesProduct() {
        ResponseEntity<Integer> response =
                testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, productId), HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1);
    }

    @Test
    @Order(16)
    void johnyDislikesAgainProduct() {
        ResponseEntity<Integer> response =
                testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, productId), HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0);
    }

    @Test
    @Order(17)
    void aliceAndJohnyDislikeProduct() {
        ResponseEntity<Integer> response1 =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, productId), HttpMethod.POST, null, Integer.class);

        ResponseEntity<Integer> response2 =
                testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, productId), HttpMethod.POST, null, Integer.class);

        assertThat(response2.getBody()).isEqualTo(2);
    }

    @Test
    @Order(18)
    void likeNonexistentProduct() {
        ResponseEntity<ErrorResponse> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_LIKE, 100), HttpMethod.POST, null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found");
    }

    @Test
    @Order(19)
    void johnyTriesToUpdateAlicesProduct() {
        ProductDTO updateRequest = new ProductDTO("New Name", 1000.00, "New Description from John!");

        HttpEntity<ProductDTO> entity = new HttpEntity<>(updateRequest);

        ResponseEntity<ErrorResponse> response =
                testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, productId), HttpMethod.PUT, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(20)
    void aliceUpdatesAlicesProduct() {
        ProductDTO updateRequest = new ProductDTO("New Name", 1000.00, "New Description from Alice!");

        HttpEntity<ProductDTO> entity = new HttpEntity<>(updateRequest);

        ResponseEntity<ProductResponseDTO> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, productId), HttpMethod.PUT, entity, ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("New Name");
        assertThat(response.getBody().getPrice()).isEqualTo(1000.00);
        assertThat(response.getBody().getDescription()).isEqualTo("New Description from Alice!");
    }

    @Test
    @Order(21)
    void johnyTriedToDeleteAlicesProduct() {
        ResponseEntity<ErrorResponse> response =
                testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, productId), HttpMethod.DELETE, null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(22)
    void aliceDeletesAlicesProduct() {
        ResponseEntity<Void> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_BY_ID, productId), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(23)
    void getNonexistentProductAfterDelete() {
        ResponseEntity<ErrorResponse> response =
                testRestTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .getForEntity(String.format(PRODUCTS_BY_ID, productId), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found");
    }

    @Test
    @Order(24)
    void loadProductsFromCSV() {
        LoadingDTO loadingDTO = new LoadingDTO(PRODUCTS_CSV);

        ResponseEntity<Void> response =
                testRestTemplate.withBasicAuth(ADMIN, ADMIN_PASS)
                        .postForEntity(PRODUCTS_LOADING, loadingDTO, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Order(25)
    void loadProductsFromURL() {
        LoadingDTO loadingDTO = new LoadingDTO(PRODUCTS_URL);

        ResponseEntity<Void> response =
                testRestTemplate.withBasicAuth(ADMIN, ADMIN_PASS)
                        .postForEntity(PRODUCTS_LOADING, loadingDTO, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
