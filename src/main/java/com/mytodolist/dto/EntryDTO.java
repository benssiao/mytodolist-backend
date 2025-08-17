package com.mytodolist.dto;

public class EntryDTO {

    private String entryBody;
    private String username;

    public EntryDTO() {
    }

    public EntryDTO(String username, String entryBody) {
        this.username = username;
        this.entryBody = entryBody;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public void setEntryBody(String entryBody) {
        this.entryBody = entryBody;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
