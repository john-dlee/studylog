package com.example.studylog.exception;

public class StudySessionNotFoundException extends RuntimeException {
    public StudySessionNotFoundException(String message) {
        super(message);
    }
}
