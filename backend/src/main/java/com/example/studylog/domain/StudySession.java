package com.example.studylog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDateTime;

@Entity
public class StudySession {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private LocalDateTime startAt;

    @Column(nullable=false)
    private LocalDateTime endedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional=true)
    @JoinColumn(name="subject_id", nullable=true)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private SessionType sessionType;

    private Integer plannedDurationMinutes;

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public void setPlannedDurationMinutes(Integer plannedDurationMinutes) {
        this.plannedDurationMinutes = plannedDurationMinutes;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public Subject getSubject() {
        return subject;
    }

    public SessionType getSessionType() {
        return sessionType;
    }
    
    public Integer getPlannedDurationMinutes() {
        return plannedDurationMinutes;
    }   

    public User getUser() {
        return user;
    }
}