package com.mytodolist.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "entries")
public class Entry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User user;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private String entryBody;

    public Entry() {
    }

    public Entry(String entryBody, User user) {
        this.entryBody = entryBody;
        this.user = user;
    }

    public Entry(String entryBody) {
        this.entryBody = entryBody;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public void setEntryBody(String entryBody) {
        this.entryBody = entryBody;
    }

    @Override
    public String toString() {
        return "Entry{"
                + "id=" + id
                + ", user=" + user
                + ", createdAt=" + createdAt
                + ", entryBody='" + entryBody + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entry)) {
            return false;
        }
        Entry entry = (Entry) o;
        return id != null && id.equals(entry.id); // Check if IDs are equal
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
