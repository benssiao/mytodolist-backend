package com.mytodolist.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mytodolist.model.Entry;
import com.mytodolist.model.User;
import com.mytodolist.repository.EntryRepository;

@Service
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
    public Entry updateEntry(Entry entry, String newBody) {

        entry.setEntryBody(newBody);
        return entryRepository.save(entry);

    }

    public Entry updateEntryById(Long entryId, String newBody) {
        Entry entry = getEntryById(entryId).orElse(null);
        if (entry != null) {
            entry.setEntryBody(newBody);
            return entryRepository.save(entry);
        }
        return null; // Entry not found
    }

    //DELETE
    public void deleteEntryById(Long entryId) {
        entryRepository.deleteById(entryId);
    }

    public void deleteEntry(Entry entry) {
        entryRepository.delete(entry);
    }

}
