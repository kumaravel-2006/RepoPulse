package com.repopulse.service;

import com.repopulse.client.GitHubApiClient;
import com.repopulse.entity.GitHubRepository;
import com.repopulse.entity.User;
import com.repopulse.repository.GitHubRepositoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class GitHubRepositoryService {

    private final GitHubApiClient githubApiClient;
    private final GitHubRepositoryRepository githubRepositoryRepository;

    public GitHubRepositoryService(
            GitHubApiClient githubApiClient,
            GitHubRepositoryRepository githubRepositoryRepository) {

        this.githubApiClient = githubApiClient;
        this.githubRepositoryRepository = githubRepositoryRepository;
    }

    public GitHubRepository saveOrGetRepository(
            String accessToken,
            User user,
            String owner,
            String repositoryName) {

        Map<String, Object> data =
                githubApiClient.getRepository(
                        accessToken,
                        owner,
                        repositoryName);

        Long githubId =
                ((Number) data.get("id")).longValue();

        return githubRepositoryRepository
                .findByGithubId(githubId)
                .orElseGet(() -> {

                    GitHubRepository repository =
                            new GitHubRepository();

                    repository.setUser(user);
                    repository.setGithubId(githubId);
                    repository.setName(
                            (String) data.get("name"));
                    repository.setFullName(
                            (String) data.get("full_name"));
                    repository.setCloneUrl(
                            (String) data.get("clone_url"));
                    repository.setDefaultBranch(
                            (String) data.get("default_branch"));
                    repository.setCreatedAt(
                            Instant.parse(
                                    (String) data.get("created_at")));
                    repository.setUpdatedAt(
                            Instant.parse(
                                    (String) data.get("updated_at")));

                    return githubRepositoryRepository
                            .save(repository);
                });
    }
    public int saveRepositories(
            String accessToken,
            User user) {

        var repositories =
                githubApiClient.getRepositories(accessToken);

        int savedCount = 0;

        for (Map<String, Object> data : repositories) {

            Long githubId =
                    ((Number) data.get("id")).longValue();

            if (githubRepositoryRepository
                    .findByGithubId(githubId)
                    .isPresent()) {

                continue;
            }

            GitHubRepository repository =
                    new GitHubRepository();

            repository.setUser(user);
            repository.setGithubId(githubId);
            repository.setName(
                    (String) data.get("name"));
            repository.setFullName(
                    (String) data.get("full_name"));
            repository.setCloneUrl(
                    (String) data.get("clone_url"));
            repository.setDefaultBranch(
                    (String) data.get("default_branch"));
            repository.setCreatedAt(
                    Instant.parse(
                            (String) data.get("created_at")));
            repository.setUpdatedAt(
                    Instant.parse(
                            (String) data.get("updated_at")));

            githubRepositoryRepository.save(repository);

            savedCount++;
        }

        return savedCount;
    }
}