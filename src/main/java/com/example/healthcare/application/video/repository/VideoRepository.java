package com.example.healthcare.application.video.repository;

import com.example.healthcare.application.video.domain.FeedbackVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.*;

public interface VideoRepository extends JpaRepository<FeedbackVideo, Long> {
    List<FeedbackVideo> findByExerciseFeedbackId(Long exerciseFeedbackId);
}
