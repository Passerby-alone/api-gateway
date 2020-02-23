package com.my.project.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author stranger_alone
 * @description TODO
 * @date 2020/2/23 下午8:40
 */
public class AuthenticationTokenImpl extends AbstractAuthenticationToken {

    private final Object principal;

    public AuthenticationTokenImpl(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return "";
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            super.setAuthenticated(true);
        }
        super.setAuthenticated(false);
    }
}
