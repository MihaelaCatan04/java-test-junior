package com.java.test.junior.controller;


import com.java.test.junior.BaseIT;
import com.java.test.junior.mapper.InteractionMapper;
import com.java.test.junior.model.InteractionKey;
import com.java.test.junior.service.DatabaseDeleteService;
import com.java.test.junior.service.DatabaseScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
class DatabaseCleanupIntegrationTest extends BaseIT {

    @Autowired
    private DatabaseScheduleService scheduleService;

    @Autowired
    private DatabaseDeleteService deleteService;

    @Autowired
    private InteractionMapper interactionMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Long USER_ID = 999L;
    private final Long PRODUCT_ID = 888L;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM product_interactions");

        jdbcTemplate.execute("INSERT INTO users (id, username, password) VALUES (" + USER_ID + ", 'testuser', 'pass') ON CONFLICT DO NOTHING");

        jdbcTemplate.execute("INSERT INTO product (id, name, price, user_id) VALUES (" + PRODUCT_ID + ", 'main_prod', 100, " + USER_ID + ") ON CONFLICT DO NOTHING");
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.execute("INSERT INTO product (id, name, price, user_id) VALUES (" + i + ", 'prod_" + i + "', 10, " + USER_ID + ") ON CONFLICT DO NOTHING");
        }
    }

    @Test
    void testFullDeleteCycle() {
        interactionMapper.upsertInteraction(USER_ID, PRODUCT_ID, true);
        interactionMapper.softDeleteInteraction(USER_ID, PRODUCT_ID);

        List<InteractionKey> keysToDelete = interactionMapper.fetchKeysToDelete(10);
        assertThat(keysToDelete).isNotEmpty();

        scheduleService.hardDeleteOldInteractions();

        List<InteractionKey> keysAfterCleanup = interactionMapper.fetchKeysToDelete(10);
        assertThat(keysAfterCleanup).isEmpty();
    }

    @Test
    void testBatchDelete() {
        for (long i = 1; i <= 5; i++) {
            interactionMapper.upsertInteraction(USER_ID, i, true);
            interactionMapper.softDeleteInteraction(USER_ID, i);
        }

        int deletedCount = deleteService.performManagedBatch(3);
        assertThat(deletedCount).isEqualTo(3);

        List<InteractionKey> remaining = interactionMapper.fetchKeysToDelete(10);
        assertThat(remaining.size()).isEqualTo(2);
    }
}