package com.sales.config.auth;

import com.sales.admin.repositories.UserRepository;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesAuthenticationManager implements AuthenticationManager {

    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();
        User user = userRepository.findByEmailAndPassword(email, password).orElseThrow(() -> new UsernameNotFoundException("Invalid Credentials !"));
        AuthUser userDetails = new SalesUser(user);
        //TODO : make sure password encrypt here..
        return new UsernamePasswordAuthenticationToken(
                userDetails, password, null
        );
    }
}
