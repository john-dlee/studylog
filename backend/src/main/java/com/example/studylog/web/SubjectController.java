package com.example.studylog.web;

import com.example.studylog.dto.SubjectRequest;
import com.example.studylog.dto.SubjectResponse;
import com.example.studylog.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService= subjectService;
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
            @Valid @RequestBody SubjectRequest subjectRequest, 
            @RequestParam Long userId) {
        SubjectResponse response = subjectService.createSubject(subjectRequest, userId);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getAll(@RequestParam Long userId) {
        return ResponseEntity.ok(subjectService.getAll(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubject(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(@PathVariable Long id, @RequestParam String newName) {
        return ResponseEntity.ok(subjectService.updateSubject(id, newName));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}