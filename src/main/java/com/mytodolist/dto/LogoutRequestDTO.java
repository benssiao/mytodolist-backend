package com.mytodolist.dto;

public class LogoutRequestDTO {

    private String username;

    public LogoutRequestDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
