package com.mytodolist.security.dtos;

public class VerifyAccessTokenRequestDTO {

    String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
