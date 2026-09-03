package com.repopulse.controller;

import com.repopulse.config.GitHubOAuthConfig;
import com.repopulse.service.GitHubOAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.repopulse.client.GitHubApiClient;
import com.repopulse.service.UserService;
import com.repopulse.entity.User;
import com.repopulse.service.GitHubRepositoryService;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
public class GitHubAuthController {

    private final GitHubOAuthConfig githubOAuthConfig;
    private final GitHubOAuthService githubOAuthService;
    private final GitHubApiClient githubApiClient;
    private final UserService userService;
    private final GitHubRepositoryService githubRepositoryService;

    public GitHubAuthController(
            GitHubOAuthConfig githubOAuthConfig,
            GitHubOAuthService githubOAuthService,
            GitHubApiClient githubApiClient,
            UserService userService,
            GitHubRepositoryService githubRepositoryService) {

        this.githubOAuthConfig = githubOAuthConfig;
        this.githubOAuthService = githubOAuthService;
        this.githubApiClient = githubApiClient;
        this.userService = userService;
        this.githubRepositoryService = githubRepositoryService;
    }

    @GetMapping("/api/auth/github")
    public ResponseEntity<Void> githubLogin() {

        String githubUrl =
                "https://github.com/login/oauth/authorize"
                        + "?client_id=" + githubOAuthConfig.getClientId()
                        + "&redirect_uri=" + githubOAuthConfig.getRedirectUri()
                        + "&scope=read:user%20repo";

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(githubUrl))
                .build();
    }
    @GetMapping("/api/auth/github/callback")
    public String githubCallback(
            @RequestParam("code") String code) {

        String accessToken =
                githubOAuthService.exchangeCodeForToken(code);

        Map<String, Object> githubUser =
                githubApiClient.getAuthenticatedUser(accessToken);

        User user =
                userService.saveOrGetUser(githubUser);

        int savedCount =
                githubRepositoryService.saveRepositories(
                        accessToken,
                        user);

        return "User saved: " + user.getUsername()
                + "\nNew repositories saved: " + savedCount;
    }
}