package com.example.studylog.dto;

import com.example.studylog.domain.SessionType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class StudySessionRequest {

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endedAt;

    @NotNull
    private SessionType sessionType;

    private Long subjectId;

    private Integer plannedDurationMinutes;

    @AssertTrue(message = "endedAt must be on or after startAt")
    public boolean isEndOnOrAfterStart() {
        if (startAt == null || endedAt == null) {
            return true;
        }
        return !endedAt.isBefore(startAt);
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getPlannedDurationMinutes() {
        return plannedDurationMinutes;
    }

    public void setPlannedDurationMinutes(Integer plannedDurationMinutes) {
        this.plannedDurationMinutes = plannedDurationMinutes;
    }
}
