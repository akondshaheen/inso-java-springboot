package com.inso.learning.taskflow.repository.jpa;

import com.inso.learning.taskflow.domain.Priority;
import com.inso.learning.taskflow.domain.TaskStatus;
import com.inso.learning.taskflow.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Demonstrates pagination/sorting (findByProjectId with a Pageable) and a
 * hand-written JPQL query (findUrgentUnfinishedTasks).
 *
 * WHAT IS JPQL, AND WHY USE @Query INSTEAD OF A LONGER QUERY METHOD NAME?
 * -------------------------------------------------------------------------
 * JPQL (Jakarta Persistence Query Language) looks like SQL, but it queries
 * our ENTITY CLASSES and their FIELDS ("TaskEntity", "t.priority") rather
 * than raw database tables and columns. Hibernate translates JPQL into
 * real SQL for us at runtime. We reach for @Query here instead of a query
 * method name because the condition we need (high priority AND not yet
 * done) combines two fields in a way that would produce an unreadably long
 * method name. ":priority" and ":excludedStatus" are named parameters,
 * bound to the method arguments via @Param - this avoids ever
 * concatenating user input directly into a query string, which is how SQL
 * injection vulnerabilities happen.
 *
 * =============================================================================
 * PERFORMANCE STAGE: THE N+1 QUERY PROBLEM, AND HOW @EntityGraph FIXES IT
 * =============================================================================
 * TaskEntity.project and TaskEntity.assignee are both declared with
 * "fetch = FetchType.LAZY" - Hibernate does NOT load the related
 * ProjectEntity/UserEntity when a TaskEntity is first fetched; it only
 * loads them the moment code actually calls task.getProject() or
 * task.getAssignee(). This is usually good for performance (we avoid
 * loading data nobody asked for), but it hides a trap: findAll() runs ONE
 * query to fetch every TaskEntity, and then, if TaskMapper (or
 * TaskResponse.from) calls task.getProject().getName() for EVERY task in
 * that list, Hibernate silently fires ONE ADDITIONAL query per task to
 * lazily load its project. For a list of 100 tasks, that is 1 query to
 * fetch the tasks, PLUS 100 more queries to fetch each task's project -
 * 101 total ("N+1") instead of just one or two. This is one of the most
 * commonly asked Java/Spring interview topics because it looks completely
 * invisible in the Java code itself and only shows up as unexpectedly slow
 * API responses.
 *
 * findAllWithProjectAndAssignee() below fixes this for the specific case
 * of listing every task: @EntityGraph tells Hibernate to load the
 * project and assignee relationships using SQL JOINs, as part of the
 * SAME single query that fetches the tasks - turning "1 + N" queries into
 * just 1. We achieve this by OVERRIDING the inherited findAll() method
 * and adding @EntityGraph to it - Spring Data JPA still provides the
 * "give me every row" behaviour, but now runs it with the extra JOINs
 * this @EntityGraph specifies. TaskRepositoryImpl.getAll() calls this
 * overridden version.
 * Notice "tags" (a @ManyToMany COLLECTION) is deliberately left OUT of
 * this entity graph: fetch-joining more than one collection at once can
 * make Hibernate return duplicate, multiplied rows (sometimes called a
 * "cartesian product"), so collections like this are usually left lazy,
 * or loaded with a second, separate query instead.
 */
public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {

    Page<TaskEntity> findByProjectId(Long projectId, Pageable pageable);

    List<TaskEntity> findByAssigneeId(Long assigneeId);

    List<TaskEntity> findByStatus(TaskStatus status);

    @Query("SELECT t FROM TaskEntity t WHERE t.priority = :priority AND t.status <> :excludedStatus")
    List<TaskEntity> findUrgentUnfinishedTasks(@Param("priority") Priority priority,
                                                @Param("excludedStatus") TaskStatus excludedStatus);

    /**
     * We deliberately OVERRIDE JpaRepository's own findAll() here (rather
     * than inventing a differently-named method) purely to attach
     * @EntityGraph to it. Spring Data JPA still recognises this as the
     * same "fetch every row" operation - it just now runs with the extra
     * JOINs this entity graph specifies.
     */
    @Override
    @EntityGraph(attributePaths = {"project", "assignee"})
    List<TaskEntity> findAll();
}
