package com.example.studylog.web;

import com.example.studylog.dto.SubjectRequest;
import com.example.studylog.dto.SubjectResponse;
import com.example.studylog.security.CurrentUser;
import com.example.studylog.service.SubjectService;
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
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final CurrentUser currentUser;

    public SubjectController(SubjectService subjectService, CurrentUser currentUser) {
        this.subjectService = subjectService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody SubjectRequest subjectRequest) {
        SubjectResponse response = subjectService.createSubject(subjectRequest, currentUser.getUserId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getAll() {
        return ResponseEntity.ok(subjectService.getAll(currentUser.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubject(id, currentUser.getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(@PathVariable Long id, @RequestParam String newName) {
        return ResponseEntity.ok(subjectService.updateSubject(id, currentUser.getUserId(), newName));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
