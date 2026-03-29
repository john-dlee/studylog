package com.example.studylog.web;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private List<Subject> subjects = new ArrayList<>();

    @PostMapping
    public Subject createSubject(@RequestBody Subject subject) {
        subjects.add(subject);
        return subject;
    }

    @GetMapping
    public List<Subject> getSubjects() {
        return subjects;
    }
}