package com.example.studylog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.studylog.domain.StudySession;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
}