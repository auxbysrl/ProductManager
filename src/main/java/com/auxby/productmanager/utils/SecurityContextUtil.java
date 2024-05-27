package com.auxby.productmanager.utils;

import com.auxby.productmanager.api.v1.user.repository.User;
import com.auxby.productmanager.exception.AuxbyAuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class SecurityContextUtil {

    private SecurityContextUtil() {
    }

    public static String getUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return null;
        }
        if (authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getPrincipal().toString();
        }

        User user = (User) authentication.getPrincipal();
        return user.getUsername();
    }

    public static User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(auth)
                || auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            throw new AuxbyAuthenticationException("Only authenticated user can perform this action.");
        }
        return (User) auth.getPrincipal();
    }
}
