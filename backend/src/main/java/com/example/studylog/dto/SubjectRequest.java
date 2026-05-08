package com.example.studylog.dto;

import jakarta.validation.constraints.NotBlank;

public class SubjectRequest {
    @NotBlank
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
