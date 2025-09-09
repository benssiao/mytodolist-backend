package com.mytodolist.dto;

public class RefreshRequestDTO {

    private String refreshToken;

    RefreshRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOldRefreshToken() {
        return refreshToken;
    }

    public void setNewRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
