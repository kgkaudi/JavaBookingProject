package com.kostas.bookingproject.auth;

import java.util.List;

public class AuthResponse {

    private String token;
    private List<String> roles;

    public AuthResponse() {
    }

    public AuthResponse(String token, List<String> roles) {
        this.token = token;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
