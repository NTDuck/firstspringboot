package com.viettelsoftware.firstspringboot.cucumber;

import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@CucumberContextConfiguration
@SpringBootTest
public class CucumberStepDefs {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private Task currentTask;
    private Task savedTask;
    private Optional<Task> foundTask;

    private User currentUser;
    private User savedUser;
    private Optional<User> foundUser;

    @Given("a task with description {string}")
    public void aTaskWithDescription(String description) {
        currentTask = Task.builder()
                .description(description)
                .build();
    }

    @When("the task is saved to the task repository")
    public void theTaskIsSavedToTheTaskRepository() {
        savedTask = taskRepository.save(currentTask);
    }

    @Then("the task can be found in the task repository by id")
    public void theTaskCanBeFoundInTheTaskRepositoryById() {
        foundTask = taskRepository.findById(savedTask.getId());
        assertTrue(foundTask.isPresent());
    }

    @And("the description of the found task should be {string}")
    public void theDescriptionOfTheFoundTaskShouldBe(String expectedDescription) {
        assertTrue(foundTask.isPresent());
        assertEquals(expectedDescription, foundTask.get().getDescription());
    }

    @When("the task is deleted from the task repository")
    public void theTaskIsDeletedFromTheTaskRepository() {
        taskRepository.deleteById(savedTask.getId());
    }

    @Then("the task should no longer exist in the task repository")
    public void theTaskShouldNoLongerExistInTheTaskRepository() {
        boolean exists = taskRepository.existsById(savedTask.getId());
        assertFalse(exists);
    }

    @Given("a user with keycloakId {string}, name {string}, email {string}, firstName {string}, lastName {string}")
    public void aUserWithFields(String keycloakId, String name, String email, String firstName, String lastName) {
        currentUser = User.builder()
                .keycloakId(keycloakId)
                .name(name)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    @When("the user is saved to the user repository")
    public void theUserIsSavedToTheUserRepository() {
        savedUser = userRepository.save(currentUser);
    }

    @Then("the user can be found in the user repository by keycloakId {string}")
    public void theUserCanBeFoundInTheUserRepositoryByKeycloakId(String keycloakId) {
        foundUser = userRepository.findByKeycloakId(keycloakId);
        assertTrue(foundUser.isPresent());
    }

    @And("the user's name should be {string}")
    public void theUserSNameShouldBe(String expectedName) {
        assertTrue(foundUser.isPresent());
        assertEquals(expectedName, foundUser.get().getName());
    }

    @Then("the user repository count should be at least {int}")
    public void theUserRepositoryCountShouldBeAtLeast(int minCount) {
        long count = userRepository.count();
        assertTrue(count >= minCount);
    }
}
