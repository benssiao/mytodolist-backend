package com.mytodolist.security.repositories;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mytodolist.security.models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r.name FROM Role r JOIN r.users u WHERE u.id = :userId")
    Set<String> findRoleNamesByUserId(@Param("userId") Long userId);

    Optional<Role> findByName(String roleName);

}
