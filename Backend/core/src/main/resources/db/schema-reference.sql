-- =============================================================================
-- STEP 3 OF OUR BUILD SEQUENCE: DATABASE SCHEMA (reference document)
-- =============================================================================
--
-- This file is NOT executed automatically by Spring Boot (our application.yml
-- uses "spring.jpa.hibernate.ddl-auto: update", which means Hibernate reads
-- our @Entity classes in the entity/ package and creates or updates these
-- exact tables for us at startup). This file exists purely so we can see, in
-- plain SQL, exactly what those annotations will produce - understanding the
-- real schema behind the Java annotations is an important interview skill.
--
-- In a real production project, we would normally NOT let Hibernate manage
-- the schema like this. Instead, we would write migration files like this
-- one and run them through a dedicated migration tool such as Flyway or
-- Liquibase, and set "ddl-auto: validate" so Hibernate only checks that our
-- entities match the schema instead of changing it. Automatic schema changes
-- on a live production database are risky (for example, a typo in an entity
-- could silently drop a column). We use "update" here only because this is
-- a learning project, so we want to move quickly without a migration step.

-- One row per user of the system. "app_user" (not "user") is used because
-- USER is a reserved SQL keyword in several databases.
CREATE TABLE app_user (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL
);

-- One row per project. "owner_id" is a foreign key: the "many" side of the
-- "one User has many Projects" relationship, so this table holds the
-- reference back to app_user.
CREATE TABLE project (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    description VARCHAR(1000),
    owner_id    BIGINT        NOT NULL REFERENCES app_user (id),
    created_at  TIMESTAMP     NOT NULL
);

-- One row per task. "project_id" is required (a task always belongs to a
-- project); "assignee_id" is optional (a task can exist before anyone is
-- assigned to it).
CREATE TABLE task (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title        VARCHAR(150)  NOT NULL,
    description  VARCHAR(1000),
    status       VARCHAR(20)   NOT NULL,
    priority     VARCHAR(20)   NOT NULL,
    due_date     DATE,
    project_id   BIGINT        NOT NULL REFERENCES project (id),
    assignee_id  BIGINT        REFERENCES app_user (id),
    created_at   TIMESTAMP     NOT NULL
);

-- One row per tag (e.g. "frontend", "urgent"). Tags exist independently of
-- any single task, because the same tag can be attached to many tasks.
CREATE TABLE tag (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- The join table for the Task <-> Tag many-to-many relationship. Neither
-- Task nor Tag can hold a single foreign key to describe this relationship,
-- because one task can have many tags AND one tag can be used on many
-- tasks. Instead, this extra table stores one row per (task, tag) pairing.
CREATE TABLE task_tags (
    task_id BIGINT NOT NULL REFERENCES task (id),
    tag_id  BIGINT NOT NULL REFERENCES tag (id),
    PRIMARY KEY (task_id, tag_id)
);

-- Indexes: a database index is a separate, sorted data structure (usually a
-- B-tree) that lets the database find matching rows in roughly O(log n)
-- time instead of scanning every row (O(n)). We add indexes here on the
-- foreign key columns we expect to filter by often (for example "show all
-- tasks for this project"), because without an index, that query would
-- perform a full table scan as the task table grows. Primary keys and
-- UNIQUE columns (like app_user.email and tag.name) already get an index
-- automatically, so we do not need to add one for those ourselves.
CREATE INDEX idx_project_owner_id ON project (owner_id);
CREATE INDEX idx_task_project_id ON task (project_id);
CREATE INDEX idx_task_assignee_id ON task (assignee_id);
