package com.mytodolist.dtos;

public class EntryDTO {

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
