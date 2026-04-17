package com.sales.interceptors;

import com.sales.wholesaler.services.WholesaleUserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class SalesHandshakeInterceptor implements HandshakeInterceptor {

    WholesaleUserService wholesaleUserService = null;

    public SalesHandshakeInterceptor(WholesaleUserService wholesaleUserService) {
        this.wholesaleUserService = wholesaleUserService;
    }

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request,
                                   @NotNull ServerHttpResponse response,
                                   @NotNull WebSocketHandler wsHandler,
                                   @NotNull Map<String, Object> attributes) {
        // Extract username from request (e.g., from a custom header)
//        String username = extractUsernameFromRequest(request);
        String userSlug = request.getURI().getQuery();
        System.err.println("The user slug beforeHandshake : "+userSlug);
        // Store username in session attributes
        if (userSlug != null) {
            attributes.put("username", userSlug);
            attributes.put("user", wholesaleUserService.findUserBySlug(userSlug));
        }

        return true; // Proceed with the handshake
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request,
                               @NotNull ServerHttpResponse response,
                               @NotNull WebSocketHandler wsHandler,
                               Exception exception) {
        // Optional: Perform actions after the handshake is complete
        log.info("The user slug afterHandshake");
    }

    private String extractUsernameFromRequest(ServerHttpRequest request) {
        // Implement your logic here to extract username from the request
        // For example, from a custom header:

//        request.getHeaders().forEach((key, value) -> safeLog.info(key + " : " + value));
        String cookieString = request.getHeaders().getFirst("cookie");
        if (cookieString != null) {
            String[] cookies = cookieString.split("; ");
            for (String cookie : cookies) {
                String[] parts = cookie.split("=");
                if (parts.length == 2 && "X-Username".equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        return null;
    }

}


