package com.example.studylog.web;

import com.example.studylog.dto.StudySessionRequest;
import com.example.studylog.dto.StudySessionResponse;
import com.example.studylog.service.StudySessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {

    private final StudySessionService studySessionService;

    public StudySessionController(StudySessionService studySessionService) {
        this.studySessionService = studySessionService;
    }

    @PostMapping
    public ResponseEntity<StudySessionResponse> create(
            @Valid @RequestBody StudySessionRequest request,
            @RequestParam Long userId) {
        StudySessionResponse response = studySessionService.create(request, userId);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StudySessionResponse>> list(@RequestParam Long userId) {
        return ResponseEntity.ok(studySessionService.listForUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySessionResponse> get(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(studySessionService.get(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudySessionResponse> update(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody StudySessionRequest request) {
        return ResponseEntity.ok(studySessionService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        studySessionService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
