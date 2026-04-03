package com.hiking.treasure.common.filter;

import com.hiking.treasure.common.web.RequestCorrelation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = RequestCorrelation.resolveOrGenerate(request.getHeader(RequestCorrelation.HEADER_NAME));
        request.setAttribute(RequestCorrelation.ATTRIBUTE_NAME, requestId);
        response.setHeader(RequestCorrelation.HEADER_NAME, requestId);
        MDC.put(RequestCorrelation.MDC_KEY, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(RequestCorrelation.MDC_KEY);
        }
    }
}
