package com.repopulse.controller;

import com.repopulse.dto.CommitInfo;
import com.repopulse.entity.GitHubRepository;
import com.repopulse.repository.GitHubRepositoryRepository;
import com.repopulse.service.GitService;
import com.repopulse.service.JavaAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.repopulse.dto.JavaFileAnalysis;
import com.repopulse.dto.JavaRepositoryAnalysis;
import com.repopulse.service.JavaRepositoryAnalysisService;
import com.repopulse.service.AnalysisPersistenceService;
import com.repopulse.entity.Analysis;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@RestController
public class GitTestController {

    private final GitService gitService;
    private final GitHubRepositoryRepository repositoryRepository;
    private final JavaAnalysisService javaAnalysisService;
    private final JavaRepositoryAnalysisService javaRepositoryAnalysisService;
    private final AnalysisPersistenceService analysisPersistenceService;

    public GitTestController(
            GitService gitService,
            GitHubRepositoryRepository repositoryRepository,
            JavaAnalysisService javaAnalysisService,
            JavaRepositoryAnalysisService javaRepositoryAnalysisService,
            AnalysisPersistenceService analysisPersistenceService) {

        this.gitService = gitService;
        this.repositoryRepository = repositoryRepository;
        this.javaAnalysisService = javaAnalysisService;
        this.javaRepositoryAnalysisService =
                javaRepositoryAnalysisService;
        this.analysisPersistenceService =
                analysisPersistenceService;
    }

    @GetMapping("/api/git/test/{id}")
    public String testGit(
            @PathVariable Long id) throws Exception {

        GitHubRepository repository =
                repositoryRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Repository not found"));

        String cloneUrl =
                repository.getCloneUrl();

        String localPath =
                "repo-test-" + id;

        File directory =
                new File(localPath);

        String path;

        if (!directory.exists()) {

            path = gitService.cloneRepository(
                    cloneUrl,
                    localPath);

        } else {

            path = directory.getAbsolutePath();
        }

        List<String> branches =
                gitService.readBranches(path);

        List<CommitInfo> commits =
                gitService.readCommits(path);

        return "Repository: "
                + repository.getFullName()
                + "\nPath: "
                + path
                + "\nBranches: "
                + branches
                + "\nCommits: "
                + commits;
    }

    @GetMapping("/api/git/analyze/{id}")
    public JavaRepositoryAnalysis analyzeRepository(@PathVariable Long id) {

        GitHubRepository repository = repositoryRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Repository not found"));

        Analysis analysisRecord =
                analysisPersistenceService
                        .createPendingAnalysis(repository);

        analysisPersistenceService.markRunning(analysisRecord);

        try {

            Path repositoryPath = Path.of("repo-test-" + id);

            if (!java.nio.file.Files.exists(repositoryPath)) {

                String clonedPath =
                        gitService.cloneRepository(
                                repository.getCloneUrl(),
                                repositoryPath.toString()
                        );

                repositoryPath = Path.of(clonedPath);
            }

            JavaRepositoryAnalysis analysis =
                    javaRepositoryAnalysisService
                            .analyzeRepository(
                                    repositoryPath,
                                    repository.getFullName()
                            );

            analysisPersistenceService.markCompleted(
                    analysisRecord,
                    analysis
            );

            return analysis;

        } catch (Exception e) {

            analysisPersistenceService.markFailed(
                    analysisRecord,
                    e.getMessage()
            );

            throw new RuntimeException(
                    "Repository analysis failed",
                    e
            );
        }
    }
}