package com.viceda_s.workout_tracker_api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.viceda_s.workout_tracker_api.BaseIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RateLimitIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${rate.limit.ip.capacity}")
    private int ipCapacity;

    @Test
    void rateLimit_WithSpoofedXForwardedFor_IgnoresSpoofedIPsAndLimitsRealIP() {
        HttpHeaders headers = new HttpHeaders();
        int allowedRequests = ipCapacity;

        for (int i = 0; i < allowedRequests; i++) {
            headers.set("X-Forwarded-For", "1.1.1." + i + ", 203.0.113.1");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange("/api/exercises", HttpMethod.GET, entity,
                    String.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        headers.set("X-Forwarded-For", "1.1.1.99, 203.0.113.1");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/exercises", HttpMethod.GET, entity,
                String.class);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
    }
}
