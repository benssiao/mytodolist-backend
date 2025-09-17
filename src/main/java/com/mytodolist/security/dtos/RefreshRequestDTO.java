package com.mytodolist.security.dtos;

public class RefreshRequestDTO {

    private String refreshToken;

    public String getOldRefreshToken() {
        return refreshToken;
    }

    public void setNewRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
