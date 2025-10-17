package com.mytodolist.security.services;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.mytodolist.models.User;
import com.mytodolist.repositories.UserRepository;
import com.mytodolist.security.models.Role;
import com.mytodolist.security.repositories.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public Set<String> getUserRoles(Long userId) {
        return roleRepository.findRoleNamesByUserId(userId);
    }

    public Role createRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public void assignRoleToUser(Long userId, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> createRole(roleName)); // if role doesnt exist, create it. doesnt matter because security only has fixed roles to check.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        role.addUser(user);
        roleRepository.save(role);
    }

    public void removeRoleFromUser(Long userId, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        role.removeUser(user);
        roleRepository.save(role);
    }

}
