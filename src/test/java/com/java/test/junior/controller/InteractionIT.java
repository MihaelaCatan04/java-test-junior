package com.java.test.junior.controller;

import com.java.test.junior.BaseIT;
import com.java.test.junior.model.ErrorResponse;
import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.model.ProductResponseDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InteractionIT extends BaseIT {

    private static final String ALICE = "alice";
    private static final String ALICE_PASS = "alice123";
    private static final String JOHNY = "johny";
    private static final String JOHNY_PASS = "johny123";
    private static final String PRODUCTS_LIKE = "/products/%d/like";
    private static final String PRODUCTS_DISLIKE = "/products/%d/dislike";

    private static final Long productId = 1L;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(2)
    void johnyLikesProduct_likeCountBecomesOne() {
        ResponseEntity<Integer> response =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_LIKE, productId),
                                HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1);
    }

    @Test
    @Order(3)
    void johnyLikesAgain_softDeletesLike_likeCountBecomesZero() {
        ResponseEntity<Integer> response =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_LIKE, productId),
                                HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0);
    }

    @Test
    @Order(4)
    void johnyLikesThirdTime_reactivatesLike_likeCountIsOne() {
        ResponseEntity<Integer> response =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_LIKE, productId),
                                HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1);
    }

    @Test
    @Order(5)
    void aliceDislikesProduct_dislikeCountBecomesOne() {
        ResponseEntity<Integer> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, productId),
                                HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(1);
    }

    @Test
    @Order(6)
    void aliceDislikesAgain_softDeletesDislike_dislikeCountBecomesZero() {
        ResponseEntity<Integer> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, productId),
                                HttpMethod.POST, null, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0);
    }

    @Test
    @Order(7)
    void switchFromLikeToDislike_replacesInteraction() {
        restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                .exchange(String.format(PRODUCTS_LIKE, productId),
                        HttpMethod.POST, null, Integer.class);

        ResponseEntity<Integer> dislikeResponse =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, productId),
                                HttpMethod.POST, null, Integer.class);

        assertThat(dislikeResponse.getBody()).isEqualTo(1);
    }

    @Test
    @Order(8)
    void switchFromDislikeToLike_replacesInteraction() {
        ResponseEntity<Integer> dislikeResponse =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, productId),
                                HttpMethod.POST, null, Integer.class);

        assertThat(dislikeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dislikeResponse.getBody()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(9)
    void dislikeNonExistentProduct_returns404() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(JOHNY, JOHNY_PASS)
                        .exchange(String.format(PRODUCTS_DISLIKE, 999999L),
                                HttpMethod.POST, null,
                                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(12)
    void unauthenticatedLike_returns401() {
        ResponseEntity<Void> response =
                restTemplate.exchange(String.format(PRODUCTS_LIKE, productId),
                        HttpMethod.POST, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}