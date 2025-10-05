package com.mytodolist.customauthtoken;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.mytodolist.models.User;
import com.mytodolist.security.services.RoleService;
import com.mytodolist.security.userdetails.TodoUserDetails;

public class CustomSecurityContextFactory
        implements WithSecurityContextFactory<WithCustomUser> {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CustomSecurityContextFactory.class);

    @Override
    public SecurityContext createSecurityContext(WithCustomUser customUser) {
        logger.info("Creating security context for user: {}", customUser.username());

        String username = customUser.username();
        String[] roles = customUser.roles();
        String password = customUser.password();

        User user = new User(username, password);
        user.setId(1L);

        RoleService roleService = mock(RoleService.class);
        when(roleService.getUserRoles(user.getId()))
                .thenReturn(Set.of(roles));

        TodoUserDetails principal = new TodoUserDetails(user, roleService);

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, password, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        logger.info("Security context created with authentication: {}", auth);
        logger.info("context = {}", context);
        return context;
    }

}
