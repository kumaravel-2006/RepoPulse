package com.repopulse.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GitHubApiClient {

    private final RestClient restClient;

    public GitHubApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }

    public Map<String, Object> getAuthenticatedUser(String accessToken) {

        return restClient
                .get()
                .uri("/user")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);
    }

    public List<Map<String, Object>> getRepositories(String accessToken) {

        return restClient
                .get()
                .uri("/user/repos")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(List.class);
    }

    public Map<String, Object> getRepository(
            String accessToken,
            String owner,
            String repositoryName) {

        return restClient
                .get()
                .uri("/repos/{owner}/{repo}", owner, repositoryName)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);
    }
}