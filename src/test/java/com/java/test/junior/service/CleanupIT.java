package com.java.test.junior.service;

import com.java.test.junior.BaseIT;
import com.java.test.junior.service.database.DatabaseScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class CleanupIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseScheduleService scheduleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String ALICE = "alice";
    private static final String ALICE_PASS = "alice123";
    private static final String PRODUCTS_LIKE = "/products/1/like";
    private static final String SOFT_DELETE_COUNT = "SELECT count(*) FROM product_interactions WHERE is_deleted = true";


    @Test
    void testFullSoftDeleteToHardDeleteLifecycle() {
        restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                .postForEntity(PRODUCTS_LIKE, null, Integer.class);

        restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                .postForEntity(PRODUCTS_LIKE, null, Integer.class);

        Integer softDeletedCount = jdbcTemplate.queryForObject(SOFT_DELETE_COUNT, Integer.class);
        assertThat(softDeletedCount).isGreaterThan(0);

        scheduleService.hardDeleteOldInteractions();

        Integer finalCount = jdbcTemplate.queryForObject(SOFT_DELETE_COUNT, Integer.class);
        assertThat(finalCount).isEqualTo(0);
    }
}