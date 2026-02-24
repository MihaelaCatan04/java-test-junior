package com.java.test.junior;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MarketplaceApplicationTests extends BaseIT {
    @Test
    void contextLoads() {
    }
}