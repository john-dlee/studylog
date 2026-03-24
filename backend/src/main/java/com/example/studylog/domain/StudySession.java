package com.example.studylog.domain;

import 

@Entity
public class StudySession {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private LocalDateTime startAt;

    @Column(nullable=false)
    private LocalDateTime endedAt;

    @ManyToOne
    @JoinColumn(name="subject_id", nullable=false)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private SessionType sessionType;


    private Integer plannedDurationMinutes;
}