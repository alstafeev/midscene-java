package com.midscene.core.yaml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the status of a task during script execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatus {

  /**
   * Name of the task.
   */
  private String taskName;

  /**
   * Current status of the task.
   */
  private Status status;

  /**
   * Current step index (0-based).
   */
  private int currentStep;

  /**
   * Total number of steps in the task.
   */
  private int totalSteps;

  /**
   * Error if the task failed.
   */
  private Exception error;

  /**
   * Error message if the task failed.
   */
  private String errorMessage;

  /**
   * Creates a new TaskStatus in INIT state.
   *
   * @param taskName   the task name
   * @param totalSteps the total number of steps
   * @return a new TaskStatus
   */
  public static TaskStatus init(String taskName, int totalSteps) {
    return TaskStatus.builder()
        .taskName(taskName)
        .status(Status.INIT)
        .currentStep(0)
        .totalSteps(totalSteps)
        .build();
  }

  /**
   * Marks the task as running.
   *
   * @return this instance
   */
  public TaskStatus start() {
    this.status = Status.RUNNING;
    return this;
  }

  /**
   * Updates the current step.
   *
   * @param step the current step index
   * @return this instance
   */
  public TaskStatus updateStep(int step) {
    this.currentStep = step;
    return this;
  }

  /**
   * Marks the task as complete.
   *
   * @return this instance
   */
  public TaskStatus complete() {
    this.status = Status.DONE;
    return this;
  }

  /**
   * Marks the task as failed with an error.
   *
   * @param error the error that occurred
   * @return this instance
   */
  public TaskStatus fail(Exception error) {
    this.status = Status.ERROR;
    this.error = error;
    this.errorMessage = error.getMessage();
    return this;
  }

  /**
   * Marks the task as skipped.
   *
   * @return this instance
   */
  public TaskStatus skip() {
    this.status = Status.SKIPPED;
    return this;
  }

  /**
   * Possible task status values.
   */
  public enum Status {
    INIT,
    RUNNING,
    DONE,
    ERROR,
    SKIPPED
  }
}
