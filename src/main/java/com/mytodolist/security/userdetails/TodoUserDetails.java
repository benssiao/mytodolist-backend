package com.mytodolist.security.userdetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mytodolist.models.User;
import com.mytodolist.security.services.RoleService;

public class TodoUserDetails implements UserDetails {

    private final User user;

    private final RoleService roleService;

    public TodoUserDetails(User user, RoleService roleService) {
        this.user = user;
        this.roleService = roleService;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> roles = roleService.getUserRoles(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new IllegalStateException("User has no roles assigned");
        }

        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Long getId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }

    public Set<String> getRoles() {
        return roleService.getUserRoles(user.getId());
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
        return true;
    }

}
