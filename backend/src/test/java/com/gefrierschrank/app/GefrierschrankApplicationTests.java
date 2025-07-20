package com.gefrierschrank.app;

import com.gefrierschrank.app.config.TestDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataInitializer.class)
class GefrierschrankApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures the application context loads successfully
    }
}