package com.example.studylog.service;

import com.example.studylog.domain.Subject;
import com.example.studylog.domain.User;
import com.example.studylog.dto.SubjectRequest;
import com.example.studylog.dto.SubjectResponse;
import com.example.studylog.exception.SubjectAlreadyExistsException;
import com.example.studylog.exception.SubjectNotFoundException;
import com.example.studylog.exception.UserNotFoundException;
import com.example.studylog.repository.SubjectRepository;
import com.example.studylog.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    public SubjectResponse createSubject(SubjectRequest subjectRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (subjectRepository.existsByNameAndUserId(subjectRequest.getName(), userId)) {
            throw new SubjectAlreadyExistsException(subjectRequest.getName() + " exists already");
        }

        Subject subject = new Subject();
        subject.setName(subjectRequest.getName());
        subject.setUser(user);

        Subject savedSubject = subjectRepository.save(subject);

        return toResponse(savedSubject);
    }

    public List<SubjectResponse> getAll(Long userId) {
        return subjectRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SubjectResponse getSubject(Long id, Long userId) {
        Subject subject = subjectRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

        return toResponse(subject);
    }

    public SubjectResponse updateSubject(Long id, Long userId, String newName) {
        Subject subject = subjectRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

        subject.setName(newName);
        subjectRepository.save(subject);

        return toResponse(subject);
    }

    public void deleteSubject(Long id, Long userId) {
        Subject subject = subjectRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));
        subjectRepository.delete(subject);
    }

    private SubjectResponse toResponse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setName(subject.getName());
        response.setId(subject.getId());
        return response;
    }
}
