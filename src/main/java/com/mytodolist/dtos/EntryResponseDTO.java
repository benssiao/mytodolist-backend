package com.mytodolist.dtos;

import java.time.Instant;

import com.mytodolist.models.Entry;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EntryResponseDTO {

    private Long id;
    @Size(max = 5000, message = "Entry body too long")
    private String entryBody;
    @NotNull(message = "Entry must be associated with a user")
    private String username;

    private Instant createdAt;

    public EntryResponseDTO(Entry entry) {
        this.id = entry.getId();

        this.entryBody = entry.getEntryBody();
        this.username = entry.getUser().getUsername();
        this.createdAt = entry.getCreatedAt();

    }

    public Long getId() {
        return id;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
