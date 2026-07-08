package com.viceda_s.workout_tracker_api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    @Value("${rate.limit.ip.capacity}")
    private int ipCapacity;

    @Value("${rate.limit.user.capacity}")
    private int userCapacity;

    private final Cache<String, Bucket> ipBuckets = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    private final Cache<String, Bucket> userBuckets = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    public Bucket resolveBucketForIp(String ip) {
        return ipBuckets.get(ip, this::newIpBucket);
    }

    public Bucket resolveBucketForUser(String username) {
        return userBuckets.get(username, this::newUserBucket);
    }

    private Bucket newIpBucket(String ip) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(ipCapacity)
                .refillIntervally(ipCapacity, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket newUserBucket(String username) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(userCapacity)
                .refillIntervally(userCapacity, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
