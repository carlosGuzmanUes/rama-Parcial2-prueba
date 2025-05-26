package com.example.rama.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            return oidcUser.getName();
        }
        return "Usuario Anónimo";
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            return oidcUser.getEmail();
        }
        return null;
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !(authentication.getPrincipal() instanceof String);
    }

    public OidcUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            return (OidcUser) authentication.getPrincipal();
        }
        return null;
    }
}
