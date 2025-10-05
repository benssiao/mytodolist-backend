package com.mytodolist.security.dtos;

public class VerifyRefreshTokenDTO {

    String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
