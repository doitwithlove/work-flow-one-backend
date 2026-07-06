package com.touchmind.work.flow.one.security;

import com.touchmind.work.flow.one.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Locale;

public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                .map(UserPrincipal::normalizeAuthority)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private static String normalizeAuthority(String role) {
        if (role == null || role.isBlank()) {
            return role;
        }

        String trimmed = role.trim();
        if (trimmed.startsWith("ROLE_")) {
            return trimmed;
        }

        return "ROLE_" + trimmed.toUpperCase(Locale.ROOT);
    }

    @Override
    public String getPassword() {

        return user.getPassword();

    }

    @Override
    public String getUsername() {

        return user.getUsername();

    }

    @Override
    public boolean isAccountNonExpired() {

        return true;

    }

    @Override
    public boolean isAccountNonLocked() {

        return true;

    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;

    }

    @Override
    public boolean isEnabled() {

        return user.isEnabled();

    }

}
