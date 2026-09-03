package com.repopulse.repository;

import com.repopulse.entity.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitHubRepositoryRepository
        extends JpaRepository<GitHubRepository, Long> {

    Optional<GitHubRepository> findByGithubId(Long githubId);
}