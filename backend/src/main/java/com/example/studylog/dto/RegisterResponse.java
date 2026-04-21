package com.example.studylog.dto;

public class RegisterResponse {
    private Long id;
    private String username;
    private String email;

    public RegisterResponse(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }
}
