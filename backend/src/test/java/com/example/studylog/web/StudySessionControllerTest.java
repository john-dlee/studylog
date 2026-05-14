package com.example.studylog.web;

import com.example.studylog.domain.SessionType;
import com.example.studylog.dto.StudySessionRequest;
import com.example.studylog.dto.StudySessionResponse;
import com.example.studylog.exception.GlobalExceptionHandler;
import com.example.studylog.exception.StudySessionNotFoundException;
import com.example.studylog.service.StudySessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudySessionController.class)
@Import(GlobalExceptionHandler.class)
class StudySessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudySessionService studySessionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postReturns400WhenSessionTypeMissing() throws Exception {
        StudySessionRequest request = new StudySessionRequest();
        request.setStartAt(LocalDateTime.parse("2026-01-10T09:00:00"));
        request.setEndedAt(LocalDateTime.parse("2026-01-10T09:30:00"));

        mockMvc.perform(post("/api/study-sessions")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postReturns400WhenEndedBeforeStart() throws Exception {
        StudySessionRequest request = new StudySessionRequest();
        request.setStartAt(LocalDateTime.parse("2026-01-10T10:00:00"));
        request.setEndedAt(LocalDateTime.parse("2026-01-10T09:00:00"));
        request.setSessionType(SessionType.POMODORO);

        mockMvc.perform(post("/api/study-sessions")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postReturns201() throws Exception {
        StudySessionRequest request = new StudySessionRequest();
        request.setStartAt(LocalDateTime.parse("2026-01-10T09:00:00"));
        request.setEndedAt(LocalDateTime.parse("2026-01-10T09:25:00"));
        request.setSessionType(SessionType.POMODORO);

        StudySessionResponse response = new StudySessionResponse();
        response.setId(100L);
        response.setStartAt(request.getStartAt());
        response.setEndedAt(request.getEndedAt());
        response.setSessionType(SessionType.POMODORO);

        when(studySessionService.create(any(StudySessionRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/study-sessions")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void getListReturns200() throws Exception {
        StudySessionResponse row = new StudySessionResponse();
        row.setId(1L);
        row.setSessionType(SessionType.STOPWATCH);
        when(studySessionService.listForUser(2L)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/study-sessions").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByIdReturns404() throws Exception {
        when(studySessionService.get(5L, 1L)).thenThrow(new StudySessionNotFoundException("Study session not found"));

        mockMvc.perform(get("/api/study-sessions/5").param("userId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void putReturns404() throws Exception {
        StudySessionRequest request = new StudySessionRequest();
        request.setStartAt(LocalDateTime.parse("2026-01-10T09:00:00"));
        request.setEndedAt(LocalDateTime.parse("2026-01-10T09:25:00"));
        request.setSessionType(SessionType.POMODORO);

        when(studySessionService.update(eq(3L), eq(1L), any(StudySessionRequest.class)))
                .thenThrow(new StudySessionNotFoundException("Study session not found"));

        mockMvc.perform(put("/api/study-sessions/3")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        doNothing().when(studySessionService).delete(7L, 1L);

        mockMvc.perform(delete("/api/study-sessions/7").param("userId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturns404() throws Exception {
        doThrow(new StudySessionNotFoundException("Study session not found"))
                .when(studySessionService).delete(7L, 1L);

        mockMvc.perform(delete("/api/study-sessions/7").param("userId", "1"))
                .andExpect(status().isNotFound());
    }
}
