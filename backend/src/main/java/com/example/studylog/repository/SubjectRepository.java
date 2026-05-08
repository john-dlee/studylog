package com.example.studylog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.studylog.domain.Subject;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByUserId(Long id);
    boolean existsByName(String name);
    boolean existsByNameAndUserId(String name, Long userId);
}