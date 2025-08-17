package com.mytodolist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mytodolist.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // freebies:    .save(), .findById(), .findAll(), .deleteById(), .delete(), .count()
}
