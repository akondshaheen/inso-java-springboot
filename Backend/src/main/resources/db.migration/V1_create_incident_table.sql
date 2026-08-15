CREATE TABLE incident(
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    detected_in_version VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP
);