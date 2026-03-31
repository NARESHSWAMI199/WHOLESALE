package com.sales.claims;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sales.entities.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SalesUser implements AuthUser {

    private final User user;

    public SalesUser(User user) {
        this.user = user;
    }

    @Override
    public int getId() {
        return user.getId();
    }

    @Override
    public String getSlug() {
        return user.getSlug();
    }

    @Override
    public String getUserType() {
        return user.getUserType();
    }

    @JsonIgnore
    @Override
    public Integer getActivePlan() {
        return user.getActivePlan();
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return user.getStatus().equals("A");
    }
}
