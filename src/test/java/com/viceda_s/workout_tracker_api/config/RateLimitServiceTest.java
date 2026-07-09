package com.viceda_s.workout_tracker_api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.bucket4j.Bucket;

public class RateLimitServiceTest {

    @Test
    void clearCaches_EmptiesAllCaches() {
        RateLimitService service = new RateLimitService(10, 100, 100);

        Bucket ipBucket1 = service.resolveBucketForIp("127.0.0.1");
        Bucket userBucket1 = service.resolveBucketForUser("test@example.com");

        service.clearCaches();

        Bucket ipBucket2 = service.resolveBucketForIp("127.0.0.1");
        Bucket userBucket2 = service.resolveBucketForUser("test@example.com");

        assertThat(ipBucket1).isNotSameAs(ipBucket2);
        assertThat(userBucket1).isNotSameAs(userBucket2);
    }

}
