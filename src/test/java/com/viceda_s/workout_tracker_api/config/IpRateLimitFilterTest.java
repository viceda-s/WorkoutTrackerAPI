package com.viceda_s.workout_tracker_api.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class IpRateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Bucket bucket;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @InjectMocks
    private IpRateLimitFilter ipRateLimitFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * An unauthenticated request that exceeds the IP limit should return 429.
     * This ensures that anonymous traffic cannot spam the API endpoints
     * (like login or register) and exhaust server resources.
     */
    @Test
    void doFilterInternal_Unauthenticated_NoTokens_Returns429()
            throws ServletException, IOException {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitService.resolveBucketForIp("192.168.1.1")).thenReturn(bucket);

        when(bucket.tryConsume(1)).thenReturn(false);

        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        ipRateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setContentType("application/problem+json");
        verify(filterChain, never()).doFilter(any(), any());
    }

    /**
     * An unauthenticated request that is within the IP limit should proceed
     * normally.
     */
    @Test
    void doFilterInternal_Unauthenticated_HasTokens_Proceeds()
            throws ServletException, IOException {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitService.resolveBucketForIp("192.168.1.1")).thenReturn(bucket);

        when(bucket.tryConsume(1)).thenReturn(true);

        ipRateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(any(Integer.class));
    }

    /**
     * An authenticated request should skip the IP rate limit entirely,
     * relying on UserRateLimitFilter instead.
     */
    @Test
    void doFilterInternal_Authenticated_SkipsIpRateLimit()
            throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        ipRateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService, never()).resolveBucketForIp(any());
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(any(Integer.class));
    }
}
