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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubjectServiceTest {
    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubjectService subjectService;

    @Test
    void shouldCreateSubjectSuccessfully() {
        User alice = new User();
        alice.setId(1L);
        alice.setUsername("alice");

        SubjectRequest request = new SubjectRequest();
        request.setName("English");

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(subjectRepository.existsByNameAndUserId("English", 1L)).thenReturn(false);
        when(subjectRepository.save(any(Subject.class))).thenAnswer(i -> {
            Subject s = i.getArgument(0);
            s.setId(100L);
            return s;
        });

        SubjectResponse result = subjectService.createSubject(request, 1L);

        assertEquals("English", result.getName());
        assertEquals(100L, result.getId());

        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void shouldFailCreateSubjectWhenUserNotFound() {
        SubjectRequest request = new SubjectRequest();
        request.setName("Math");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> subjectService.createSubject(request, 99L));
    }

    @Test
    void shouldFailCreateSubjectWhenNameExistsForUser() {
        User alice = new User();
        alice.setId(1L);

        SubjectRequest request = new SubjectRequest();
        request.setName("English");

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(subjectRepository.existsByNameAndUserId("English", 1L)).thenReturn(true);

        assertThrows(SubjectAlreadyExistsException.class, () -> subjectService.createSubject(request, 1L));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoSubjects() {
        when(subjectRepository.findByUserId(1L)).thenReturn(List.of());

        List<SubjectResponse> result = subjectService.getAll(1L);

        assertEquals(0, result.size());
    }

    @Test
    void shouldReturnAllSubjectsForUser() {
        Subject s1 = new Subject();
        s1.setId(10L);
        s1.setName("A");
        Subject s2 = new Subject();
        s2.setId(11L);
        s2.setName("B");

        when(subjectRepository.findByUserId(1L)).thenReturn(List.of(s1, s2));

        List<SubjectResponse> result = subjectService.getAll(1L);

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals(10L, result.get(0).getId());
        assertEquals("B", result.get(1).getName());
        assertEquals(11L, result.get(1).getId());
    }

    @Test
    void shouldGetSubjectById() {
        Subject subject = new Subject();
        subject.setId(5L);
        subject.setName("Physics");

        when(subjectRepository.findById(5L)).thenReturn(Optional.of(subject));

        SubjectResponse result = subjectService.getSubject(5L);

        assertEquals("Physics", result.getName());
        assertEquals(5L, result.getId());
    }

    @Test
    void shouldFailGetSubjectWhenNotFound() {
        when(subjectRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () -> subjectService.getSubject(5L));
    }

    @Test
    void shouldUpdateSubject() {
        Subject subject = new Subject();
        subject.setId(3L);
        subject.setName("Old");

        when(subjectRepository.findById(3L)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);

        SubjectResponse result = subjectService.updateSubject(3L, "New");

        assertEquals("New", result.getName());
        assertEquals(3L, result.getId());
        verify(subjectRepository).save(subject);
    }

    @Test
    void shouldFailUpdateSubjectWhenNotFound() {
        when(subjectRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () -> subjectService.updateSubject(3L, "New"));
    }

    @Test
    void shouldDeleteSubject() {
        Subject subject = new Subject();
        subject.setId(7L);

        when(subjectRepository.findById(7L)).thenReturn(Optional.of(subject));

        subjectService.deleteSubject(7L);

        verify(subjectRepository).delete(subject);
    }

    @Test
    void shouldFailDeleteSubjectWhenNotFound() {
        when(subjectRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () -> subjectService.deleteSubject(7L));
    }
}
