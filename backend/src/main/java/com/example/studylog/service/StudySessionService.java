package com.example.studylog.service;

import com.example.studylog.domain.StudySession;
import com.example.studylog.domain.Subject;
import com.example.studylog.domain.User;
import com.example.studylog.dto.StudySessionRequest;
import com.example.studylog.dto.StudySessionResponse;
import com.example.studylog.exception.StudySessionNotFoundException;
import com.example.studylog.exception.SubjectNotFoundException;
import com.example.studylog.exception.UserNotFoundException;
import com.example.studylog.repository.StudySessionRepository;
import com.example.studylog.repository.SubjectRepository;
import com.example.studylog.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public StudySessionService(
            StudySessionRepository studySessionRepository,
            UserRepository userRepository,
            SubjectRepository subjectRepository) {
        this.studySessionRepository = studySessionRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    public StudySessionResponse create(StudySessionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Subject subject = resolveSubjectForUser(userId, request.getSubjectId());

        StudySession session = new StudySession();
        session.setUser(user);
        session.setSubject(subject);
        session.setStartAt(request.getStartAt());
        session.setEndedAt(request.getEndedAt());
        session.setSessionType(request.getSessionType());
        session.setPlannedDurationMinutes(request.getPlannedDurationMinutes());

        StudySession saved = studySessionRepository.save(session);
        return toResponse(saved);
    }

    public List<StudySessionResponse> listForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        return studySessionRepository.findByUser_IdOrderByStartAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public StudySessionResponse get(Long sessionId, Long userId) {
        StudySession session = studySessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new StudySessionNotFoundException("Study session not found"));
        return toResponse(session);
    }

    public StudySessionResponse update(Long sessionId, Long userId, StudySessionRequest request) {
        StudySession session = studySessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new StudySessionNotFoundException("Study session not found"));

        Subject subject = resolveSubjectForUser(userId, request.getSubjectId());

        session.setSubject(subject);
        session.setStartAt(request.getStartAt());
        session.setEndedAt(request.getEndedAt());
        session.setSessionType(request.getSessionType());
        session.setPlannedDurationMinutes(request.getPlannedDurationMinutes());

        studySessionRepository.save(session);
        return toResponse(session);
    }

    public void delete(Long sessionId, Long userId) {
        StudySession session = studySessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new StudySessionNotFoundException("Study session not found"));
        studySessionRepository.delete(session);
    }

    private Subject resolveSubjectForUser(Long userId, Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found"));
        if (!subject.getUser().getId().equals(userId)) {
            throw new SubjectNotFoundException("Subject not found");
        }
        return subject;
    }

    private StudySessionResponse toResponse(StudySession session) {
        StudySessionResponse response = new StudySessionResponse();
        response.setId(session.getId());
        response.setStartAt(session.getStartAt());
        response.setEndedAt(session.getEndedAt());
        response.setSessionType(session.getSessionType());
        response.setPlannedDurationMinutes(session.getPlannedDurationMinutes());
        Subject subject = session.getSubject();
        if (subject != null) {
            response.setSubjectId(subject.getId());
            response.setSubjectName(subject.getName());
        }
        return response;
    }
}
