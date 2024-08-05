package com.example.healthcare.application.exercise.domain;

import com.example.healthcare.application.account.domain.User;
import com.example.healthcare.application.exercise.service.dto.ExerciseFeedbackDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "exercise_feedback_id")
    private Long id;

    //피드백 요청 유저
    private Long userId;
    //피드백 응답 트레이너
    private Long trainerId;
    //피드백 요청 유저의 메시지
    @Column(name = "feedback_message")
    private String feedbackMessage;

    public ExerciseFeedback(Long userId, ExerciseFeedbackDTO dto){
        this.userId = userId;
        this.trainerId = dto.trainer_id();
        this.feedbackMessage = dto.feedbackMessage();
    }
}
