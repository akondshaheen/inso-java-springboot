package com.inso.learning.taskflow.domain;

/**
 * =============================================================================
 * STEP 4 OF OUR BUILD SEQUENCE: THE DOMAIN LAYER
 * =============================================================================
 *
 * WHY DO WE HAVE A SEPARATE "DOMAIN" PACKAGE, DIFFERENT FROM "entity"?
 * -------------------------------------------------------------------------
 * The classes in this package represent our BUSINESS CONCEPTS in plain
 * Java, with absolutely no framework annotations (no @Entity, no @Column).
 * The classes in the "entity" package (created next) represent the exact
 * same real-world things, but shaped specifically for JPA/Hibernate to
 * persist them to a relational database.
 *
 * Keeping these separate is a common pattern once a codebase grows, for a
 * few practical reasons:
 *   1. Our business logic (in the service layer) works only with domain
 *      objects, so it stays completely independent of how - or even
 *      whether - we use a relational database at all. We could swap
 *      Hibernate for a different persistence technology later, and only
 *      the entity classes and mappers would need to change.
 *   2. JPA sometimes forces small shape compromises on entities (like the
 *      protected no-argument constructor Hibernate needs), which have
 *      nothing to do with business rules. Domain classes stay free of that.
 *   3. It makes the responsibility of each layer explicit: entity = "how do
 *      we store this?", domain = "what does this mean for the business?".
 *
 * For a small learning project like this one, a single shared model would
 * also be a perfectly reasonable choice, and many real teams do exactly
 * that to avoid extra mapping code. We use two models here specifically
 * because it is a widely asked interview topic and lets us practice
 * writing an explicit entity <-> domain mapper (see the mapper/ package).
 *
 * An enum like this one is used because a User's role can only ever be one
 * of a small, fixed set of values - the compiler will reject any other
 * value, which eliminates typo bugs like "ADMN" that a plain String field
 * would happily accept. Under the hood, USER and ADMIN are each a single
 * shared instance of this class, which is why enum values can safely be
 * compared with "==" as well as with ".equals()".
 */
public enum Role {
    USER,
    ADMIN
}
