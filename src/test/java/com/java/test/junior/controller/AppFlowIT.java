package com.java.test.junior.controller;

import com.java.test.junior.BaseIT;
import com.java.test.junior.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.StatusResultMatchersExtensionsKt.isEqualTo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppFlowIT extends BaseIT {
    private static final String ALICE = "alice";
    private static final String ALICE_PASS = "alice123";
    private static final String JOHNY = "johny";
    private static final String JOHNY_PASS = "johny123";
    private static final String ADMIN = System.getProperty("ADMIN_USERNAME");
    private static final String ADMIN_PASS = System.getProperty("ADMIN_PASSWORD");
    private static Long productId;
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @Order(1)
    void aliceRegistration() {
        UserRegistrationDTO request = new UserRegistrationDTO(ALICE, ALICE_PASS);
        ResponseEntity<UserResponseDTO> response = testRestTemplate.postForEntity("/auth/register", request, UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getUsername()).isEqualTo(ALICE);
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    @Order(2)
    void johnyRegistration() {
        UserRegistrationDTO request = new UserRegistrationDTO(JOHNY, JOHNY_PASS);
        ResponseEntity<UserResponseDTO> response = testRestTemplate.postForEntity("/auth/register", request, UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getUsername()).isEqualTo(JOHNY);
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    @Order(3)
    void rejectDuplicateRegistration() {
        UserRegistrationDTO request = new UserRegistrationDTO(ALICE, "anotherPass");
        ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity("/auth/register", request, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage()).isEqualTo("User already exists!");
    }

    @Test
    @Order(4)
    void unauthenticatedUserCannotCreateProduct() {
        ProductDTO request = new ProductDTO("Test Product", 100.00, "Test description");
        ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity("/products", request, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    void aliceCreatesProduct() {
        ProductDTO request = new ProductDTO("Test Product", 100.00, "Test description");
        ResponseEntity<ProductResponseDTO> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).postForEntity("/products", request, ProductResponseDTO.class);
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
        ResponseEntity<ProductResponseDTO> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).getForEntity("/products/" + productId, ProductResponseDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getName()).isEqualTo("Test Product");
        assertThat(response.getBody().getPrice()).isEqualTo(100.00);
        assertThat(response.getBody().getDescription()).isEqualTo("Test description");
        assertThat(response.getBody().getUsername()).isEqualTo(ALICE);
    }

    @Test
    @Order(7)
    void getNonexistentProductById() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).getForEntity("/products/100", ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found");
    }

    @Test
    @Order(8)
    void getProductByNegativeId() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).getForEntity("/products/-1", ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Product id must be positive");
    }

    @Test
    @Order(9)
    void getProductByName() {
        ResponseEntity<List<ProductResponseDTO>> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).exchange("/products/name/Test", HttpMethod.GET, null, new ParameterizedTypeReference<List<ProductResponseDTO>>() {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @Order(10)
    void createProductsForPagination() {
        for (int i = 0; i < 4; i++) {
            ProductDTO request = new ProductDTO("Product " + i, 100.00, "Test description");
            ResponseEntity<ProductResponseDTO> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).postForEntity("/products", request, ProductResponseDTO.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Test
    @Order(11)
    void getFirstPage() {
        ResponseEntity<PageResponse<ProductResponseDTO>> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).exchange("/products?page=1&page_size=2", HttpMethod.GET, null, new ParameterizedTypeReference<PageResponse<ProductResponseDTO>>() {
        });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<ProductResponseDTO> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getContent().size()).isEqualTo(2);
        assertThat(body.getTotalElements()).isEqualTo(5L);
        assertThat(body.getTotalPages()).isEqualTo(3);
        ;
    }

    @Test
    @Order(12)
    void getInexistentPage() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).getForEntity("/products?page=100&page_size=2", ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Page 100 does not exist");
    }

    @Test
    @Order(13)
    void johnyLikesProduct() {
        ResponseEntity<Integer> response = testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS).exchange("/products/" + productId + "/like", HttpMethod.POST, null, Integer.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1);
    }

    @Test
    @Order(14)
    void johnyLikesAgainProduct() {
        ResponseEntity<Integer> response = testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS).exchange("/products/" + productId + "/like", HttpMethod.POST, null, Integer.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0);
    }

    @Test
    @Order(15)
    void johnyDislikesProduct() {
        ResponseEntity<Integer> response = testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS).exchange("/products/" + productId + "/dislike", HttpMethod.POST, null, Integer.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1);
    }

    @Test
    @Order(16)
    void johnyDislikesAgainProduct() {
        ResponseEntity<Integer> response = testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS).exchange("/products/" + productId + "/dislike", HttpMethod.POST, null, Integer.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0);
    }

    @Test
    @Order(17)
    void aliceAndJohnyDislikeProduct() {
        ResponseEntity<Integer> response1 = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).exchange("/products/" + productId + "/dislike", HttpMethod.POST, null, Integer.class);
        ResponseEntity<Integer> response2 = testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS).exchange("/products/" + productId + "/dislike", HttpMethod.POST, null, Integer.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isEqualTo(2);
    }

    @Test
    @Order(18)
    void likeNonexistentProduct() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).exchange("/products/100/like", HttpMethod.POST, null, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found");
    }

    @Test
    @Order(19)
    void johnyTriesToUpdateAlicesProduct() {
        ProductDTO updateRequest = new ProductDTO("New Name", 1000.00, "New Description from John!");
        ResponseEntity<ErrorResponse> response = testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS).exchange("/products/" + productId, HttpMethod.PUT, new ResponseEntity<>(updateRequest, null, HttpStatus.OK), ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(20)
    void aliceUpdatesAlicesProduct() {
        ProductDTO updateRequest = new ProductDTO("New Name", 1000.00, "New Description from Alice!");
        ResponseEntity<ProductResponseDTO> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).exchange("/products/" + productId, HttpMethod.PUT, new ResponseEntity<>(updateRequest, null, HttpStatus.OK), ProductResponseDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("New Name");
        assertThat(response.getBody().getPrice()).isEqualTo(1000.00);
        assertThat(response.getBody().getDescription()).isEqualTo("New Description from Alice!");
    }

    @Test
    @Order(21)
    void johnyTriedToDeleteAlicesProduct() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.withBasicAuth(JOHNY, JOHNY_PASS).exchange("/products/" + productId, HttpMethod.DELETE, null, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(22)
    void aliceDeletesAlicesProduct() {
        ResponseEntity<Void> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).exchange("/products/" + productId, HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(23)
    void getNonexistentProductAfterDelete() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.withBasicAuth(ALICE, ALICE_PASS).getForEntity("/products/" + productId, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Product not found");
    }

    @Test
    @Order(24)
    void loadProductsFromCSV() {
        LoadingDTO loadingDTO = new LoadingDTO("src/test/resources/products.csv");

        ResponseEntity<Void> response = testRestTemplate.withBasicAuth(ADMIN, ADMIN_PASS).postForEntity("/products/loading/products", loadingDTO, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }


    @Test
    @Order(25)
    void loadProductsFromURL() {
        LoadingDTO loadingDTO = new LoadingDTO("https://raw.githubusercontent.com/MihaelaCatan04/java-test-junior/main/src/main/resources/products.csv");

        ResponseEntity<Void> response = testRestTemplate.withBasicAuth(ADMIN, ADMIN_PASS).postForEntity("/products/loading/products", loadingDTO, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }


}
