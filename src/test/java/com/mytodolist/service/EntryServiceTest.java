package com.mytodolist.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mytodolist.models.Entry;
import com.mytodolist.models.User;
import com.mytodolist.repositories.EntryRepository;
import com.mytodolist.repositories.UserRepository;
import com.mytodolist.services.EntryService;

@ExtendWith(MockitoExtension.class)
public class EntryServiceTest {

    @Mock
    private EntryRepository entryRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private EntryService entryService;

    @Test
    public void testCreateEntry() {

        Entry entryToBeCreated = new Entry("Test entry body", new User("testuser", "password"));
        entryToBeCreated.setId(1L);
        entryToBeCreated.setCreatedAt(LocalDateTime.now());
        when(entryRepository.save(any(Entry.class))).thenReturn(entryToBeCreated);
        assertThat(entryService.createEntry(entryToBeCreated, new User("testuser", "password"))).isEqualTo(entryToBeCreated);
        verify(entryRepository).save(any(Entry.class));

    }

    @Test
    public void testGetEntriesByUser() {
        User user = new User("testuser", "password");
        Entry entry1 = new Entry("Test entry 1", user);
        Entry entry2 = new Entry("Test entry 2", user);
        when(entryRepository.findByUser(user)).thenReturn(List.of(entry1, entry2));
        List<Entry> entries = entryService.getEntriesByUser(user);
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getEntryBody()).isEqualTo("Test entry 1");
        assertThat(entries.get(1).getEntryBody()).isEqualTo("Test entry 2");

        verify(entryRepository).findByUser(user);

    }

    @Test
    public void testGetEntriesByNonExistentUser() {
        User nonExistentUser = new User("username", "password");
        nonExistentUser.setId(999L);
        when(entryRepository.findByUser(nonExistentUser)).thenReturn(new ArrayList<>());

        assertThat(entryService.getEntriesByUser(nonExistentUser)).isEmpty();
        verify(entryRepository).findByUser(nonExistentUser);

    }

    @Test
    public void testGetEntryById() {
        Entry entry = new Entry("Test entry body");
        entry.setId(1L);
        entry.setCreatedAt(LocalDateTime.now());
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        assertThat(entryService.getEntryById(1L)).isPresent().get().isEqualTo(entry);
        verify(entryRepository).findById(1L);
    }

    @Test
    public void testUpdateEntryById() {
        User user = new User("testuser", "password");
        Entry oldEntry = new Entry("Old entry body", user);
        oldEntry.setId(1L);
        oldEntry.setCreatedAt(LocalDateTime.now());

        Entry newEntry = new Entry("Updated entry body", user);
        newEntry.setId(1L);
        newEntry.setCreatedAt(LocalDateTime.now());

        when(entryRepository.findById(1L)).thenReturn(Optional.of(oldEntry));
        when(entryRepository.save(any(Entry.class))).thenReturn(newEntry);

        Entry updatedEntry = entryService.updateEntryById(1L, "Updated entry body");
        assertThat(updatedEntry.getEntryBody()).isEqualTo("Updated entry body");
        assertThat(updatedEntry.getId()).isEqualTo(1L);

        verify(entryRepository).save(any(Entry.class));

    }

    @Test
    public void testDeleteEntryById() {
        doNothing().when(entryRepository).deleteById(1L); // Mocking deleteById method
        entryService.deleteEntryById(1L);
        verify(entryRepository).deleteById(1L);

    }

}
