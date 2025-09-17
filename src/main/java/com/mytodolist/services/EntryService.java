package com.mytodolist.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mytodolist.models.Entry;
import com.mytodolist.models.User;
import com.mytodolist.repositories.EntryRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class EntryService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EntryService.class);
    private final EntryRepository entryRepository;

    public EntryService(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    //CREATE
    public Entry createEntry(Entry entry, User user) {
        logger.info("Creating entry for user: {}", user.getUsername());
        logger.info("Entry body: {}", entry.getEntryBody());
        entry.setUser(user);
        return entryRepository.save(entry);
    }

    //READ
    public List<Entry> getEntriesByUser(User user) {
        return entryRepository.findByUser(user);
    }

    public Optional<Entry> getEntryById(Long entryId) {
        return entryRepository.findById(entryId);
    }

    //UPDATE
    public Entry updateEntryById(Long entryId, String newBody) {
        Entry entry = getEntryById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Entry with id " + entryId + " not found"));
        entry.setEntryBody(newBody);
        return entryRepository.save(entry);
    }

    //DELETE
    public void deleteEntryById(Long entryId) {
        entryRepository.deleteById(entryId);
    }

}
