Feature: TaskRepository operations

  Scenario: Save and find a Task
    Given a task with description "Cucumber Task"
    When the task is saved to the task repository
    Then the task can be found in the task repository by id
    And the description of the found task should be "Cucumber Task"

  Scenario: Delete a Task
    Given a task with description "Task To Delete"
    When the task is saved to the task repository
    And the task is deleted from the task repository
    Then the task should no longer exist in the task repository
