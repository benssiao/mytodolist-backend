package com.mytodolist.service;

import com.mytodolist.security.repositories.RoleRepository;
import com.mytodolist.repositories.UserRepository;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.mytodolist.security.services.RoleService;
import org.mockito.InjectMocks;
import com.mytodolist.security.models.Role;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

import static org.mockito.Mockito.times;
import com.mytodolist.models.User;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private RoleService roleService;

    @Test
    void testGetUserRoles() {
        when(roleRepository.findRoleNamesByUserId(1L)).thenReturn(Set.of("ROLE_USER"));
        Set<String> roles = roleService.getUserRoles(1L);
        assertThat(roles).contains("ROLE_USER");
        verify(roleRepository).findRoleNamesByUserId(1L);
    }

    @Test
    void testCreateRole() {
        Role role = new Role();
        role.setName("ROLE_USER");
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        Role createdRole = roleService.createRole("ROLE_USER");
        assertEquals("ROLE_USER", createdRole.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testAssignRoleToUserRoleExists() {
        User newUser = new User("testuser", "password");
        Long userId = 1L;
        newUser.setId(userId);
        Role role = new Role();
        String roleName = "ROLE_USER";
        role.setName(roleName);

        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User("testuser", "password")));
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        roleService.assignRoleToUser(userId, roleName);
        assertThat(role.getUsers()).hasSize(1);
        assertThat(role.getUsers().contains(roleName));

        verify(roleRepository, times(1)).save(role);
        verify(roleRepository, times(1)).findByName(roleName);
        verify(userRepository, times(1)).findById(userId);

    }

    @Test
    void testAssignRoleToUserRoleDoesNotExist() {
        User newUser = new User("testuser", "password");
        Long userId = 1L;
        newUser.setId(userId);
        Role role = new Role();
        String roleName = "ROLE_USER";
        role.setName(roleName);

        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User("testuser", "password")));

        roleService.assignRoleToUser(userId, roleName);
        assertThat(role.getUsers()).hasSize(1);
        assertThat(role.getUsers().contains(roleName));
        verify(roleRepository, times(1)).save(role);
        verify(roleRepository, times(1)).findByName(roleName);
        verify(userRepository, times(1)).findById(userId);
    }

}
