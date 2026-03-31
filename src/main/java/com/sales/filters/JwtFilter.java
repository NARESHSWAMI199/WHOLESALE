package com.sales.filters;

import com.sales.admin.repositories.StorePermissionsRepository;
import com.sales.admin.repositories.UserRepository;
import com.sales.cachemanager.services.UserCacheService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.config.UnAuthorizedPaths;
import com.sales.entities.User;
import com.sales.global.GlobalConstant;
import com.sales.global.USER_TYPES;
import com.sales.jwtUtils.JwtToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtToken jwtUtil;
    private final UserRepository userRepository;
    private final StorePermissionsRepository storePermissionsRepository;
    private final UserCacheService userCacheService;


    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return Arrays.stream(UnAuthorizedPaths.unAuthorizePaths)
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(GlobalConstant.AUTHORIZATION);
        log.info("The request token : {}", authHeader);
        if (authHeader != null && authHeader.startsWith(GlobalConstant.AUTH_TOKEN_PREFIX)) {
            String token = authHeader.substring(7);
            String slug = jwtUtil.getSlugFromToken(token);
            if (slug != null) {
                MDC.put("userSlug", slug);
            }
            User user = userCacheService.getCacheUser(slug);
            if (user == null) {
                user = userRepository.findUserBySlug(slug);
                List<GrantedAuthority> authorities = grantedAuthorities(user);
                user.setAuthorities(authorities);
                userCacheService.saveCacheUser(user);
            }
            log.info("The request user : {}", user);
            AuthUser userDetails = new SalesUser(user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, slug, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);

    }


    private List<GrantedAuthority> grantedAuthorities(User user) {
        Set<String> permissions = new HashSet<>();
        if (user.getUserType().equals(USER_TYPES.STAFF.getType()) || user.getUserType().equals(USER_TYPES.SUPER_ADMIN.getType())) {
            permissions = userRepository.findAllPermissionsByUserId(user.getId());
        } else if (user.getUserType().equals(USER_TYPES.WHOLESALER.getType())) {
            permissions = storePermissionsRepository.getAllAssignedPermissionByUserId(user.getId());
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        permissions.forEach(permission -> {
            authorities.add(new SimpleGrantedAuthority(permission));
        });
        return authorities;
    }

}