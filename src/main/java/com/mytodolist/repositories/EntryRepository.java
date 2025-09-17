package com.mytodolist.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mytodolist.models.Entry;
import com.mytodolist.models.User;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    List<Entry> findByUserId(Long userId); // finds all entries for a specific user id

    List<Entry> findByUser(User user); // finds all entries for a specific user

    List<Entry> findByUserId(Long userId, Pageable pageable);

    List<Entry> findByUser(User user, Pageable pageable);

    long countByUserId(Long userId);

    // freebies:    .save(), .findById(), .findAll(), .deleteById(), .delete(), .count()
}
