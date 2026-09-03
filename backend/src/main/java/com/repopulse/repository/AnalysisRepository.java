package com.repopulse.repository;

import com.repopulse.entity.Analysis;
import com.repopulse.entity.AnalysisStatus;
import com.repopulse.entity.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    List<Analysis> findByRepository(GitHubRepository repository);

    List<Analysis> findByStatus(AnalysisStatus status);
}