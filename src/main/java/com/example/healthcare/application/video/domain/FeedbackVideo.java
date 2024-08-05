package com.example.healthcare.application.video.domain;

import com.example.healthcare.application.exercise.domain.Exercise;
import com.example.healthcare.application.exercise.domain.ExerciseFeedback;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

public class FeedbackVideo {
    @Id
    @Column(name = "feedback_video_uuid")
    private String feedbackVideoUuid;

    // url 최대길이: 2083
    @Column(name = "feedback_video", nullable = false, length = 2084)
    private String videoUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @JsonBackReference
    private Exercise exercise;

    @ManyToOne
    @JoinColumn(name = "exercise_feedback_id", nullable = false)
    @JsonBackReference
    private ExerciseFeedback exerciseFeedback;

    public FeedbackVideo(String videoKey, String videoUrl, Exercise exercise, ExerciseFeedback exerciseFeedback){

    }
}
