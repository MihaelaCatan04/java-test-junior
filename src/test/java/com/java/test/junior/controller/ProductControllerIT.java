package com.java.test.junior.controller;

import com.java.test.junior.BaseIntegrationTest;
import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.model.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProductControllerIT extends BaseIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static TestRestTemplate authRestTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("TRUNCATE TABLE product RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
    }

    private TestRestTemplate getAuthRestTemplate(String username, String password) {
        UserDTO user = new UserDTO();
        user.setUsername(username);
        user.setPassword(password);
        restTemplate.postForEntity("/auth/register", user, Void.class);
        return restTemplate.withBasicAuth(username, password);
    }

    @Test
    void testCreateProductAuthenticated() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);

        ResponseEntity<ProductDTO> response = authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    }

    @Test
    void testCreateProductUnauthorized() {
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);

        ResponseEntity<ProductDTO> response = restTemplate.postForEntity("/products", product, ProductDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testGetProductWhenExists() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);

        ResponseEntity<ProductDTO> response = restTemplate.getForEntity("/products/1", ProductDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void testGetProductWhenDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity("/products/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateProductAuthenticated() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(19.99);
        authRestTemplate.put("/products/1", updatedProduct);

        ResponseEntity<ProductDTO> response = restTemplate.getForEntity("/products/1", ProductDTO.class);

        assertNotNull(response.getBody());
        assertThat(response.getBody().getName()).isEqualTo("Updated Product");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void testUpdateProductUnauthorized() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);

        ResponseEntity<ProductDTO> createResponse = authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(19.99);

        HttpEntity<ProductDTO> request = new HttpEntity<>(updatedProduct);
        ResponseEntity<Void> updateResponse = restTemplate.exchange("/products/1", HttpMethod.PUT, request, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testDeleteProductAuthenticated() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        authRestTemplate.delete("/products/1");
        ResponseEntity<String> response = restTemplate.getForEntity("/products/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteProductUnauthorized() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<Void> updateResponse = restTemplate.exchange("/products/1", HttpMethod.DELETE, null, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testGetProductsPaginatedAuthenticated() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<List> response = authRestTemplate.getForEntity("/products?page=1&page_size=1", List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testGetProductsPaginatedUnauthorized() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<List<ProductDTO>> response = restTemplate.exchange("/products?page=1&page_size=1", HttpMethod.GET, null, new ParameterizedTypeReference<List<ProductDTO>>() {
        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    void testGetProductByNameAuthenticated() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("TestProduct");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<ProductDTO> response = authRestTemplate.getForEntity("/products/name/TestProduct", ProductDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testGetProductByNameUnauthorized() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("TestProduct");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<ProductDTO> response = restTemplate.getForEntity("/products/name/TestProduct", ProductDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testLikeProductAuthenticated() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<Void> likeResponse = authRestTemplate.postForEntity("/products/1/like", null, Void.class);
        assertThat(likeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testLikeProductUnauthorized() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<Void> likeResponse = restTemplate.postForEntity("/products/1/like", null, Void.class);
        assertThat(likeResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testDislikeProductAuthenticated() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<Void> dislikeResponse = authRestTemplate.postForEntity("/products/1/dislike", null, Void.class);
        assertThat(dislikeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testDislikeProductUnauthorized() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<Void> dislikeResponse = restTemplate.postForEntity("/products/1/dislike", null, Void.class);
        assertThat(dislikeResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    //    @Test
//    void testLoadProductsFromCSV() {
//        jdbcTemplate.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'pass', 'ADMIN')");
//        TestRestTemplate authRestTemplate = getAuthRestTemplate("testuser", "password");
//
//        LoadingDTO loadingDTO = new LoadingDTO();
//        String path = new File("src/test/resources/products.csv").getAbsolutePath();
//        loadingDTO.setFileAddress(path);
//
//        ResponseEntity<Void> response = authRestTemplate.postForEntity("/products/loading/products", loadingDTO, Void.class);
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//    }
    @Test
    void testGetADeletedProduct() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        authRestTemplate.delete("/products/1");
        ResponseEntity<String> response = authRestTemplate.getForEntity("/products/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testLikeADeletedProduct() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        authRestTemplate.delete("/products/1");
        ResponseEntity<String> response = authRestTemplate.postForEntity("/products/1/like", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDislikeADeletedProduct() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        authRestTemplate.delete("/products/1");
        ResponseEntity<String> response = authRestTemplate.postForEntity("/products/1/dislike", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testLikeAProductThatIsNotLiked() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<String> response = authRestTemplate.postForEntity("/products/1/like", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testDislikeAProductThatIsNotDisliked() {
        authRestTemplate = getAuthRestTemplate("testuser", "password");
        ProductDTO product = new ProductDTO();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(9.99);
        authRestTemplate.postForEntity("/products", product, ProductDTO.class);
        ResponseEntity<String> response = authRestTemplate.postForEntity("/products/1/dislike", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}




