package com.example.studylog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.studylog.domain.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}