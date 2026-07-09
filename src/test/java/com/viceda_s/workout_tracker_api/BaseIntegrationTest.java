package com.viceda_s.workout_tracker_api;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.viceda_s.workout_tracker_api.config.RateLimitService;

@SpringBootTest
public abstract class BaseIntegrationTest {

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("workout_test_db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RateLimitService rateLimitService;

    @AfterEach
    void teardown() {
        jdbcTemplate.execute("TRUNCATE TABLE workout_exercises, workout_plans, exercises, users CASCADE");
        rateLimitService.clearCaches();
    }

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("jwt.secret", () -> "very_long_and_secure_secret_key_for_testing_only");
    }

}
