package com.example.studylog.dto;

public class ForgotPasswordResponse {

    private final String message;
    private final String resetLink;

    public ForgotPasswordResponse(String message, String resetLink) {
        this.message = message;
        this.resetLink = resetLink;
    }

    public String getMessage() {
        return message;
    }

    public String getResetLink() {
        return resetLink;
    }
}
