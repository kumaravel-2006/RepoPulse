package com.repopulse.service;

import com.repopulse.config.GitHubOAuthConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GitHubOAuthService {

    private final GitHubOAuthConfig githubOAuthConfig;
    private final RestClient restClient;

    public GitHubOAuthService(GitHubOAuthConfig githubOAuthConfig) {
        this.githubOAuthConfig = githubOAuthConfig;
        this.restClient = RestClient.builder().build();
    }

    public String exchangeCodeForToken(String code) {

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();

        formData.add(
                "client_id",
                githubOAuthConfig.getClientId()
        );

        formData.add(
                "client_secret",
                githubOAuthConfig.getClientSecret()
        );

        formData.add(
                "code",
                code
        );

        Map<String, Object> response = restClient
                .post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(formData)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException(
                    "GitHub did not return an access token"
            );
        }

        return response.get("access_token").toString();
    }

}