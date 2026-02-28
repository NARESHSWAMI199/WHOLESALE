package com.sales.interceptors;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sales.admin.repositories.StorePermissionsRepository;
import com.sales.admin.repositories.UserRepository;
import com.sales.cachemanager.services.UserCacheService;
import com.sales.request.ErrorDto;
import com.sales.entities.User;
import com.sales.global.GlobalConstant;
import com.sales.global.USER_TYPES;
import com.sales.jwtUtils.JwtToken;
import com.sales.utils.Utils;
import com.sales.wholesaler.services.WholesaleServicePlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SalesInterceptor implements HandlerInterceptor {

    
    private final Logger logger = LoggerFactory.getLogger(SalesInterceptor.class);

    private final JwtToken jwtToken;
    private final UserRepository userRepository;

    private final StorePermissionsRepository storePermissionsRepository;
    private final WholesaleServicePlanService wholesaleServicePlanService;
    private final UserCacheService userCacheService;

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Token from swagger because swagger not sends Authorization header in request.
        token = token == null ? request.getHeader("authToken") : token;
        logger.debug("request url : {}", request.getRequestURI());
        try {
        if (token != null && token.startsWith(GlobalConstant.AUTH_TOKEN_PREFIX)) {
            token = token.substring(7);
            String slug = jwtToken.getSlugFromToken(token);
            /* get user by slug. */
            User user = userCacheService.getCacheUser(slug);
            user = Objects.nonNull(user) ? user : userRepository.findUserBySlug(slug);
            String requestUrI = request.getRequestURI();
            Set<String> permittedUrls = null;
            if(user.getUserType().equals("S")  || user.getUserType().equals("SA")){
                permittedUrls = userRepository.findAllPermissionsByUserId(user.getId());
                requestUrI = requestUrI.replaceAll("detail","all");
            }else if(user.getUserType().equals(USER_TYPES.WHOLESALER.getType())){
                permittedUrls = storePermissionsRepository.getAllAssignedPermissionByUserId(user.getId());
            }

            if (user.getIsDeleted().equals("Y")) {
                sendError(response,"User is not found.",401);
                return false;
            } else if (user.getStatus().equals("D")) {
                sendError(response,"User is not active.",401);
                return false;
            }
            else if (user.getUserType().equals("W") && !wholesaleServicePlanService.isPlanActive(user.getActivePlan())){
                sendError(response,"You don't have any active plan.",403);
                return false;
            }

            /*if user have list permission also has detail permission */

            boolean isPermitted = false;
            for( String permission : Objects.requireNonNull(permittedUrls)) {
                if(requestUrI.contains(permission) && !Utils.isEmpty(permission)){
                    isPermitted = true;
                    break;
                }
            }
//            if (!isPermitted && user.getId() != GlobalConstant.suId) {
//                sendError(response, "You don't permissions to access "+requestUrI+".Please contact your administrator.", 400);
//                return false;
//            }
//            request.setAttribute("user",user);
            return true;
        }
        sendError(response,"Invalid authorization.",401);
        return false;
        }catch (Exception e){
            sendError(response,e.getMessage(),401);
            return false;
        }
    }

    public void sendError(HttpServletResponse response ,String message, Integer status) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");
        response.setStatus(status);
        ErrorDto error = new ErrorDto(message,status);
        response.getWriter().write(mapper.writeValueAsString(error));
    }





}
