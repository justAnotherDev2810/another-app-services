package com.microservice.job.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs every incoming HTTP request and outgoing response.
 *
 * Output example:
 *   --> POST /api/user
 *   <-- POST /api/user | 201 | 45ms
 *
 * This is what lets you see in real time what's being called and how long
 * it takes — no more guessing why something is slow or silently failing.
 *
 * Activated automatically via @Component — Spring picks it up as a filter
 * as long as the package is scanned.
 */
@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.info("--> {} {}", method, uri);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("<-- {} {} | {} | {}ms", method, uri, response.getStatus(), duration);
        }
    }
}