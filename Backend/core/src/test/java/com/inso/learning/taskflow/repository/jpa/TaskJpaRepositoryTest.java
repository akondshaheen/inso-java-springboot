package com.inso.learning.taskflow.repository.jpa;

import com.inso.learning.taskflow.domain.Priority;
import com.inso.learning.taskflow.domain.Role;
import com.inso.learning.taskflow.domain.TaskStatus;
import com.inso.learning.taskflow.entity.ProjectEntity;
import com.inso.learning.taskflow.entity.TaskEntity;
import com.inso.learning.taskflow.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * =============================================================================
 * @DataJpaTest: AN INTEGRATION TEST FOR THE JPA REPOSITORY LAYER
 * =============================================================================
 *
 * WHY IS THIS AN INTEGRATION TEST, NOT A UNIT TEST?
 * -------------------------------------------------------------------------
 * A unit test checks one small piece of Java logic in isolation, usually by
 * mocking away its dependencies. Here, we deliberately do NOT mock
 * anything - @DataJpaTest boots a real (in-memory H2) database, a real
 * Hibernate EntityManager, and real Spring Data JPA repository proxies.
 * This is exactly the point: we want to prove that our @Entity mappings,
 * relationships, and JPQL query actually work against a real database
 * engine, which a mocked repository could never verify.
 *
 * WHAT DOES @DataJpaTest DO FOR US?
 * -------------------------------------------------------------------------
 * It configures only the slice of the Spring application relevant to JPA
 * (repositories, the EntityManager, the DataSource) instead of starting
 * the entire application context (no web server, no controllers, no
 * services) - this keeps the test fast. It also wraps every test method in
 * a transaction that is rolled back automatically at the end, so tests
 * never leave leftover data behind for the next test to trip over.
 *
 * @ActiveProfiles("test") switches application.yml to our H2 in-memory
 * configuration (create-drop) instead of the real Postgres database, so
 * this test can run anywhere - a laptop, a CI server - with zero setup.
 */
@DataJpaTest
@ActiveProfiles("test")
class TaskJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ProjectJpaRepository projectJpaRepository;

    @Autowired
    private TaskJpaRepository taskJpaRepository;

    @Test
    void savedTaskCanBeFoundByProjectIdWithPagination() {
        UserEntity owner = userJpaRepository.save(new UserEntity("Alice", "alice@example.com", "hash", Role.ADMIN));
        ProjectEntity project = projectJpaRepository.save(new ProjectEntity("Website Revamp", "Redesign the site", owner));

        TaskEntity task = new TaskEntity("Set up CI pipeline", "Automate the build", Priority.HIGH,
                LocalDate.now().plusDays(3), project);
        taskJpaRepository.save(task);

        Pageable firstPage = PageRequest.of(0, 10);
        List<TaskEntity> tasksForProject = taskJpaRepository.findByProjectId(project.getId(), firstPage).getContent();

        assertThat(tasksForProject).hasSize(1);
        assertThat(tasksForProject.get(0).getTitle()).isEqualTo("Set up CI pipeline");
    }

    @Test
    void jpqlQueryFindsOnlyHighPriorityTasksThatAreNotDone() {
        UserEntity owner = userJpaRepository.save(new UserEntity("Bob", "bob@example.com", "hash", Role.USER));
        ProjectEntity project = projectJpaRepository.save(new ProjectEntity("Mobile App", "Build the app", owner));

        TaskEntity urgentTask = new TaskEntity("Fix crash on login", "Users cannot log in", Priority.HIGH,
                LocalDate.now().plusDays(1), project);
        TaskEntity doneUrgentTask = new TaskEntity("Old fixed bug", "Already resolved", Priority.HIGH,
                LocalDate.now().minusDays(1), project);
        doneUrgentTask.setStatus(TaskStatus.DONE);
        TaskEntity lowPriorityTask = new TaskEntity("Update README", "Minor documentation change", Priority.LOW,
                LocalDate.now().plusDays(10), project);

        taskJpaRepository.save(urgentTask);
        taskJpaRepository.save(doneUrgentTask);
        taskJpaRepository.save(lowPriorityTask);

        List<TaskEntity> urgentUnfinished = taskJpaRepository.findUrgentUnfinishedTasks(Priority.HIGH, TaskStatus.DONE);

        assertThat(urgentUnfinished).hasSize(1);
        assertThat(urgentUnfinished.get(0).getTitle()).isEqualTo("Fix crash on login");
    }
}
