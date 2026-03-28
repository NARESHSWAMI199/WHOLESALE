package com.sales.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private String getFriendlyMessage(AuthenticationException ex) {
        if (ex.getCause() instanceof ExpiredJwtException) {
            return "Token has expired. Please login again.";
        }
        if (ex.getCause() instanceof MalformedJwtException || ex.getCause() instanceof SignatureException) {
            return "Invalid token signature or format.";
        }
        if (ex instanceof BadCredentialsException) {
            return "Invalid username or password.";
        }
        return "You are not authenticated. " + ex.getMessage();
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", getFriendlyMessage(authException));
        body.put("path", request.getServletPath());
        body.put("timestamp", Instant.now().toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), body);
    }
}