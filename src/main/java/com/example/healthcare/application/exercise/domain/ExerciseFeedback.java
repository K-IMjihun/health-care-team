package com.example.healthcare.application.exercise.domain;

import com.example.healthcare.application.exercise.service.dto.ExerciseFeedbackDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "exercise_feedback_id")
    private Long id;

    //피드백 요청 유저

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "exercise_id", nullable = false)
//    @JsonBackReference
//    private Exercise exercise;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "feedback_message")
    private String feedbackMessage;

    public ExerciseFeedback(ExerciseFeedbackDTO dto){
        this.videoUrl = dto.videoUrl();
        this.feedbackMessage = dto.feedbackMessage();
    }
}
