package com.inso.learning.taskflow.repository.jpa;

import com.inso.learning.taskflow.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * =============================================================================
 * STEP 8 OF OUR BUILD SEQUENCE: SPRING DATA JPA REPOSITORY
 * =============================================================================
 *
 * WHAT HAPPENS WHEN A METHOD ON THIS INTERFACE IS CALLED?
 * -------------------------------------------------------------------------
 * This is an INTERFACE with no implementation body. Spring Data JPA scans
 * for interfaces like this one that extend JpaRepository at startup, and
 * for each one it generates a real implementing class at runtime using a
 * Java feature called a "dynamic proxy". That generated class is
 * registered as a Spring bean.
 *
 * When code calls, say, "userJpaRepository.findByEmail(...)":
 *   1. The call hits the dynamically generated proxy.
 *   2. Spring Data parses the METHOD NAME ("findByEmail") into a query:
 *      "find a UserEntity where email = ?1" - purely from the method name,
 *      no SQL or JPQL is written for this particular method.
 *   3. Hibernate turns that into real SQL ("select * from app_user where
 *      email = ?"), using the EntityManager/Session it manages internally.
 *   4. Hibernate executes the SQL through a JDBC connection obtained from
 *      the connection pool, maps each result row back into a UserEntity,
 *      and returns it wrapped in an Optional (a matching row might not
 *      exist).
 *
 * This interface (and the entities it works with) stay in the "jpa"
 * sub-package on purpose - it is an implementation detail of the entity
 * <-> database bridge. Our own UserRepository interface (the one the
 * service layer actually depends on) works with domain.User instead;
 * UserRepositoryImpl is the only class that talks to both.
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
