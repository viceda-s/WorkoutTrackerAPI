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
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
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
public class UserRateLimitFilterTest {

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
    private UserRateLimitFilter userRateLimitFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * An authenticated request that exceeds the user's specific rate limit should
     * return 429.
     * This ensures that a single logged-in user cannot abuse the API by sending too
     * many
     * requests and exhausting their personal bucket capacity.
     */
    @Test
    void doFilterInternal_Authenticated_NoTokens_Returns429()
            throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("vince@example.com");

        when(rateLimitService.resolveBucketForUser("vince@example.com")).thenReturn(bucket);

        when(bucket.tryConsume(1)).thenReturn(false);

        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        userRateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setContentType("application/problem+json");
        verify(filterChain, never()).doFilter(any(), any());
    }

    /**
     * An authenticated request that is within the user's specific rate limit
     * should proceed normally through the filter chain.
     */
    @Test
    void doFilterInternal_Authenticated_HasTokens_Proceeds()
            throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("vince@example.com");

        when(rateLimitService.resolveBucketForUser("vince@example.com")).thenReturn(bucket);

        when(bucket.tryConsume(1)).thenReturn(true);

        userRateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(any(Integer.class));
    }
}
