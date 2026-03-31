package com.sales.claims;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface AuthUser {

    int getId();

    String getSlug();

    String getUserType();

    Integer getActivePlan();

    Collection<? extends GrantedAuthority> getAuthorities();

    String getPassword();

    String getUsername();

    String getEmail();

    boolean isEnabled();
}
