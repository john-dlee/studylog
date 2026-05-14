package com.example.studylog.repository;

import com.example.studylog.domain.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByUser_IdOrderByStartAtDesc(Long userId);

    Optional<StudySession> findByIdAndUser_Id(Long id, Long userId);
}