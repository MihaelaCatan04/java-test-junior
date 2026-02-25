package com.java.test.junior.service;

import com.java.test.junior.BaseToxiproxyIT;
import com.java.test.junior.service.database.DatabaseScheduleService;
import com.java.test.junior.service.database.DatabaseScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseResilienceIT extends BaseToxiproxyIT {

    private static final long SCENARIO1_SLEEP_MS = 3000;
    private static final long SCENARIO2_INITIAL_SLEEP_MS = 1000;
    private static final long SCENARIO2_RECOVERY_SLEEP_MS = 3000;

    private static final long SCENARIO1_TIMEOUT_SECONDS = 15;
    private static final long SCENARIO2_TIMEOUT_SECONDS = 20;

    private static final long SCENARIO3_MAX_DURATION_MS = 5000;
    private static final long SCENARIO3_EXPECTED_MAX_ELAPSED_MS = 8000;

    private static final int PRODUCT_COUNT = 50;
    private static final double DEFAULT_PRICE = 10.0;

    private static final String DELETE_PRODUCT_INTERACTIONS_SQL = "DELETE FROM product_interactions";

    private static final String DELETE_PRODUCT_SQL = "DELETE FROM product";

    private static final String DELETE_USERS_SQL = "DELETE FROM users";

    private static final String INSERT_USER_SQL = "INSERT INTO users (id, username, password) VALUES (1, 'tester', 'pass')";

    private static final String INSERT_PRODUCT_SQL = "INSERT INTO product (id, name, price, user_id) VALUES (?, ?, ?, 1)";

    private static final String SEED_INTERACTIONS_SQL = """
            INSERT INTO product_interactions
            (user_id, product_id, is_like, is_deleted, delete_attempts)
            VALUES (1, ?, true, true, 0)
            """;

    private static final String COUNT_SOFT_DELETED_SQL = "SELECT COUNT(*) FROM product_interactions WHERE is_deleted = true";

    @Autowired
    private DatabaseScheduleService scheduleService;

    @Autowired
    private DatabaseScheduleServiceImpl scheduleServiceImpl;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        restoreConnection();

        jdbcTemplate.update(DELETE_PRODUCT_INTERACTIONS_SQL);
        jdbcTemplate.update(DELETE_PRODUCT_SQL);
        jdbcTemplate.update(DELETE_USERS_SQL);

        jdbcTemplate.update(INSERT_USER_SQL);

        for (int i = 1; i <= PRODUCT_COUNT; i++) {
            jdbcTemplate.update(INSERT_PRODUCT_SQL, i, "Product " + i, DEFAULT_PRICE);
        }
    }

    @Test
    void downAtStart_thenRecovers() throws Exception {
        seedInteractions(10);
        cutConnection();

        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> scheduleService.hardDeleteOldInteractions());

        Thread.sleep(SCENARIO1_SLEEP_MS);
        restoreConnection();

        task.get(SCENARIO1_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(countSoftDeleted()).isEqualTo(0);
    }

    @Test
    void failsMidRun_thenRecovers() throws Exception {
        seedInteractions(20);

        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> scheduleService.hardDeleteOldInteractions());

        Thread.sleep(SCENARIO2_INITIAL_SLEEP_MS);
        cutConnection();

        Thread.sleep(SCENARIO2_RECOVERY_SLEEP_MS);
        restoreConnection();

        task.get(SCENARIO2_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(countSoftDeleted()).isEqualTo(0);
    }

    @Test
    void permanentFailure_exitsGracefully() throws Exception {
        setMaxDuration();

        seedInteractions(10);
        cutConnection();

        long start = System.currentTimeMillis();
        scheduleService.hardDeleteOldInteractions();
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(SCENARIO3_EXPECTED_MAX_ELAPSED_MS);

        restoreConnection();
        assertThat(countSoftDeleted()).isGreaterThan(0);
    }

    private void seedInteractions(int count) {
        for (int i = 1; i <= count; i++) {
            jdbcTemplate.update(SEED_INTERACTIONS_SQL, i);
        }
    }

    private int countSoftDeleted() {
        Integer count = jdbcTemplate.queryForObject(COUNT_SOFT_DELETED_SQL, Integer.class);
        return count != null ? count : 0;
    }

    private void setMaxDuration() {
        ReflectionTestUtils.setField(scheduleServiceImpl, "maxDurationMillis", DatabaseResilienceIT.SCENARIO3_MAX_DURATION_MS);
    }
}