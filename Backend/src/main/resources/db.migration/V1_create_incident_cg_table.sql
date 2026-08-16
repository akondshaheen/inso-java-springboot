CREATE TABLE incident_cg(
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) UNIQUE,
    country VARCHAR(255),
    category VARCHAR(255),
    severity VARCHAR(255),
    description VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP
);