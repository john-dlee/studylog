package com.example.studylog.web;

import com.example.studylog.dto.StudySessionRequest;
import com.example.studylog.dto.StudySessionResponse;
import com.example.studylog.security.CurrentUser;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {

    private final StudySessionService studySessionService;
    private final CurrentUser currentUser;

    public StudySessionController(StudySessionService studySessionService, CurrentUser currentUser) {
        this.studySessionService = studySessionService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<StudySessionResponse> create(@Valid @RequestBody StudySessionRequest request) {
        StudySessionResponse response = studySessionService.create(request, currentUser.getUserId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StudySessionResponse>> list() {
        return ResponseEntity.ok(studySessionService.listForUser(currentUser.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySessionResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(studySessionService.get(id, currentUser.getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudySessionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StudySessionRequest request) {
        return ResponseEntity.ok(studySessionService.update(id, currentUser.getUserId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studySessionService.delete(id, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
