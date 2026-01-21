package com.sales.filters;

import com.sales.admin.repositories.StorePermissionsRepository;
import com.sales.admin.repositories.UserRepository;
import com.sales.cachemanager.services.UserCacheService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
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


    /* Paths which no need to authenticate */
    String [] unAuthorizePaths = {"/admin/auth/login",
            "/admin/auth/login/otp",
            "/admin/auth/sendOtp",
            "/admin/auth/register",
            "/wholesale/auth/login",
            "/wholesale/auth/register",
            "/wholesale/auth/login/otp",
            "/wholesale/auth/sendOtp",
            "/wholesale/auth/register",
            "/webjars/**",
            "/admin/auth/profile/**",
            "/wholesale/auth/profile/**",
            "/admin/store/image/**",
            "/admin/item/image/**",
            "/pg/callback/**",
            "/cashfree/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/plans/**",
            "/wholesale/address/state",
            "/wholesale/address/city/**",
            "/wholesale/store/category/**",
            "/wholesale/store/subcategory/**",
            "/wholesale/auth/validate-otp",
            "/wholesale/plan/all",
            "/admin/auth/profile/**",
            "/index",
            "/chat2",
            "/chat/images/**",
            "/js/**",
            "/css/**",
            "/images/**",
            /* Paths which need to be authenticated but don't need to check in Interceptor due to some different conditions */
            "/wholesale/plan/my-plans",
            "/wholesale/plan/is-active",
            "/pg/pay/**",
            "/wholesale/store/add",
            "/wholesale/auth/detail",
            "/future/plans/**",
            "/wholesale/wallet/**"
    };
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return Arrays.stream(unAuthorizePaths)
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(GlobalConstant.AUTHORIZATION);
        log.info("The request token : {}",authHeader);
        if (authHeader != null && authHeader.startsWith(GlobalConstant.AUTH_TOKEN_PREFIX)) {
            String token = authHeader.substring(7);
            String slug = jwtUtil.getSlugFromToken(token);
            if (slug != null) {
                MDC.put("userSlug", slug);
            }
            User user = userCacheService.getCacheUser(slug);
            if(user == null){
                user = userRepository.findUserBySlug(slug);
                List<GrantedAuthority> authorities = grantedAuthorities(user);
                user.setAuthorities(authorities);
                userCacheService.saveCacheUser(user);
            }
            log.info("The request user : {}",user);
            AuthUser userDetails = new SalesUser(user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, slug, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);

    }


    private List<GrantedAuthority> grantedAuthorities(User user){
        Set<String> permissions = new HashSet<>();
        if(user.getUserType().equals(USER_TYPES.STAFF.getType())  || user.getUserType().equals(USER_TYPES.SUPER_ADMIN.getType())){
            permissions = userRepository.findAllPermissionsByUserId(user.getId());
        }else if(user.getUserType().equals(USER_TYPES.WHOLESALER.getType())){
            permissions = storePermissionsRepository.getAllAssignedPermissionByUserId(user.getId());
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        permissions.forEach(permission -> {
            authorities.add(new SimpleGrantedAuthority(permission));
        });
        return authorities;
    }

}