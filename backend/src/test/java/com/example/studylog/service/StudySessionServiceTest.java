package com.example.studylog.service;

import com.example.studylog.domain.SessionType;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private StudySessionService studySessionService;

    private final LocalDateTime start = LocalDateTime.of(2026, 1, 10, 9, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 1, 10, 9, 25);

    @Test
    void createSucceedsWithoutSubject() {
        User user = new User();
        user.setId(1L);

        StudySessionRequest request = baseRequest();
        request.setSubjectId(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(studySessionRepository.save(any(StudySession.class))).thenAnswer(inv -> {
            StudySession s = inv.getArgument(0);
            s.setId(50L);
            return s;
        });

        StudySessionResponse response = studySessionService.create(request, 1L);

        assertEquals(50L, response.getId());
        assertNull(response.getSubjectId());
        ArgumentCaptor<StudySession> captor = ArgumentCaptor.forClass(StudySession.class);
        verify(studySessionRepository).save(captor.capture());
        assertEquals(user, captor.getValue().getUser());
        assertNull(captor.getValue().getSubject());
    }

    @Test
    void createSucceedsWithSubjectOwnedByUser() {
        User user = new User();
        user.setId(1L);
        Subject subject = new Subject();
        subject.setId(10L);
        subject.setUser(user);

        StudySessionRequest request = baseRequest();
        request.setSubjectId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
        when(studySessionRepository.save(any(StudySession.class))).thenAnswer(inv -> {
            StudySession s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        StudySessionResponse response = studySessionService.create(request, 1L);

        assertEquals(10L, response.getSubjectId());
        assertEquals(2L, response.getId());
    }

    @Test
    void createFailsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> studySessionService.create(baseRequest(), 1L));
    }

    @Test
    void createFailsWhenSubjectMissing() {
        User user = new User();
        user.setId(1L);
        StudySessionRequest request = baseRequest();
        request.setSubjectId(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(subjectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () -> studySessionService.create(request, 1L));
    }

    @Test
    void createFailsWhenSubjectBelongsToAnotherUser() {
        User owner = new User();
        owner.setId(1L);
        User other = new User();
        other.setId(2L);
        Subject subject = new Subject();
        subject.setId(10L);
        subject.setUser(other);

        StudySessionRequest request = baseRequest();
        request.setSubjectId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));

        assertThrows(SubjectNotFoundException.class, () -> studySessionService.create(request, 1L));
    }

    @Test
    void listFailsWhenUserMissing() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> studySessionService.listForUser(1L));
    }

    @Test
    void listReturnsEmpty() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(studySessionRepository.findByUser_IdOrderByStartAtDesc(1L)).thenReturn(List.of());

        assertEquals(0, studySessionService.listForUser(1L).size());
    }

    @Test
    void getFailsWhenNotFound() {
        when(studySessionRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.empty());
        assertThrows(StudySessionNotFoundException.class, () -> studySessionService.get(5L, 1L));
    }

    @Test
    void updateFailsWhenNotFound() {
        when(studySessionRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.empty());
        assertThrows(StudySessionNotFoundException.class, () -> studySessionService.update(5L, 1L, baseRequest()));
    }

    @Test
    void deleteFailsWhenNotFound() {
        when(studySessionRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.empty());
        assertThrows(StudySessionNotFoundException.class, () -> studySessionService.delete(5L, 1L));
    }

    @Test
    void deleteRemovesSession() {
        User user = new User();
        user.setId(1L);
        StudySession session = new StudySession();
        session.setId(7L);
        session.setUser(user);

        when(studySessionRepository.findByIdAndUser_Id(7L, 1L)).thenReturn(Optional.of(session));

        studySessionService.delete(7L, 1L);

        verify(studySessionRepository).delete(session);
    }

    private StudySessionRequest baseRequest() {
        StudySessionRequest request = new StudySessionRequest();
        request.setStartAt(start);
        request.setEndedAt(end);
        request.setSessionType(SessionType.POMODORO);
        request.setPlannedDurationMinutes(25);
        return request;
    }
}
