CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE repositories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    github_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    clone_url VARCHAR(500) NOT NULL,
    default_branch VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_repositories_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE TABLE analyses (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    risk_score DECIMAL(5,2),
    error_message TEXT,

    CONSTRAINT fk_analyses_repository
        FOREIGN KEY (repository_id)
        REFERENCES repositories(id)
);