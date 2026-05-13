package com.example.studylog.web;

import com.example.studylog.dto.SubjectRequest;
import com.example.studylog.dto.SubjectResponse;
import com.example.studylog.exception.GlobalExceptionHandler;
import com.example.studylog.exception.SubjectAlreadyExistsException;
import com.example.studylog.exception.SubjectNotFoundException;
import com.example.studylog.service.SubjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

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

@WebMvcTest(SubjectController.class)
@Import(GlobalExceptionHandler.class)
public class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubjectService subjectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        SubjectRequest request = new SubjectRequest();
        request.setName("");

        mockMvc.perform(post("/api/subjects")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn201WhenCreateSucceeds() throws Exception {
        SubjectRequest request = new SubjectRequest();
        request.setName("English");

        SubjectResponse response = new SubjectResponse();
        response.setId(10L);
        response.setName("English");

        when(subjectService.createSubject(any(SubjectRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/subjects")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("English"));
    }

    @Test
    void shouldReturn409WhenSubjectAlreadyExists() throws Exception {
        SubjectRequest request = new SubjectRequest();
        request.setName("English");

        when(subjectService.createSubject(any(SubjectRequest.class), eq(1L)))
                .thenThrow(new SubjectAlreadyExistsException("English exists already"));

        mockMvc.perform(post("/api/subjects")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void shouldReturn200WhenGetAll() throws Exception {
        SubjectResponse r = new SubjectResponse();
        r.setId(1L);
        r.setName("Math");
        when(subjectService.getAll(2L)).thenReturn(List.of(r));

        mockMvc.perform(get("/api/subjects").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Math"));
    }

    @Test
    void shouldReturn200WhenGetById() throws Exception {
        SubjectResponse r = new SubjectResponse();
        r.setId(5L);
        r.setName("Physics");
        when(subjectService.getSubject(5L)).thenReturn(r);

        mockMvc.perform(get("/api/subjects/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Physics"));
    }

    @Test
    void shouldReturn404WhenGetByIdNotFound() throws Exception {
        when(subjectService.getSubject(5L)).thenThrow(new SubjectNotFoundException("Subject not found"));

        mockMvc.perform(get("/api/subjects/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void shouldReturn200WhenUpdate() throws Exception {
        SubjectResponse r = new SubjectResponse();
        r.setId(3L);
        r.setName("NewName");
        when(subjectService.updateSubject(3L, "NewName")).thenReturn(r);

        mockMvc.perform(put("/api/subjects/3").param("newName", "NewName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    void shouldReturn404WhenUpdateNotFound() throws Exception {
        when(subjectService.updateSubject(3L, "X")).thenThrow(new SubjectNotFoundException("Subject not found"));

        mockMvc.perform(put("/api/subjects/3").param("newName", "X"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn204WhenDelete() throws Exception {
        doNothing().when(subjectService).deleteSubject(7L);

        mockMvc.perform(delete("/api/subjects/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeleteNotFound() throws Exception {
        doThrow(new SubjectNotFoundException("Subject not found")).when(subjectService).deleteSubject(7L);

        mockMvc.perform(delete("/api/subjects/7"))
                .andExpect(status().isNotFound());
    }
}
