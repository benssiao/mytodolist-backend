package com.mytodolist.dtos;

import jakarta.validation.constraints.Size;

public class EntryDTO {

    @Size(max = 5000, message = "Entry cannot exceed 5000 characters")
    private String entryBody;

    public EntryDTO() {
    }

    public EntryDTO(String entryBody) {

        this.entryBody = entryBody;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public void setEntryBody(String entryBody) {
        this.entryBody = entryBody;
    }

}
