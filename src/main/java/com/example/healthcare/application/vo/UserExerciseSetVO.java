package com.example.healthcare.application.vo;

import com.example.healthcare.application.exercise.domain.code.ExerciseSetType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserExerciseSetVO(
  Long routineId,
  Long setId,
  Long setNumber,
  ExerciseSetType exerciseSetType,
  Double weight,
  Integer reps,
  Long time,
  boolean complete
) {

  @QueryProjection
  public UserExerciseSetVO(Long routineId, Long setId, Long setNumber, ExerciseSetType exerciseSetType, Double weight, Integer reps, Long time, boolean complete) {
    this.routineId = routineId;
    this.setId = setId;
    this.setNumber = setNumber;
    this.exerciseSetType = exerciseSetType;
    this.weight = weight;
    this.reps = reps;
    this.time = time;
    this.complete = complete;
  }
}
