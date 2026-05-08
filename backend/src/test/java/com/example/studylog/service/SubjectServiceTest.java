package com.example.studylog.service;

import com.example.studylog.domain.Subject;
import com.example.studylog.domain.User;
import com.example.studylog.dto.RegisterRequest;
import com.example.studylog.dto.SubjectRequest;
import com.example.studylog.dto.SubjectResponse;
import com.example.studylog.repository.SubjectRepository;
import com.example.studylog.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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


    private SubjectRequest request;
    private RegisterRequest registerRequest;

    @Test
    void shouldCreateSubjectSuccessfully() {
        User alice = new User();
        alice.setId(1L);
        alice.setUsername("alice");

        request = new SubjectRequest();
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


}
