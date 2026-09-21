package com.repopulse.service;

import com.repopulse.analysis.git.FileOwnership;
import com.repopulse.dto.ContributorAnalysis;
import com.repopulse.dto.FileChurn;
import com.repopulse.dto.GitCommitAnalysis;
import com.repopulse.dto.HotspotAnalysis;
import com.repopulse.dto.JavaClassAnalysis;
import com.repopulse.dto.JavaFileAnalysis;
import com.repopulse.dto.JavaMethodAnalysis;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GitHistoryService {

    // ============================================================
    // 9.1 — Commit Frequency
    // ============================================================

    public GitCommitAnalysis analyzeCommitHistory(
            Path repositoryPath) {

        try (Git git = Git.open(repositoryPath.toFile())) {

            int totalCommits = 0;

            Iterable<RevCommit> commits =
                    git.log().call();

            for (RevCommit commit : commits) {
                totalCommits++;
            }

            return new GitCommitAnalysis(totalCommits);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to analyze Git history",
                    e
            );
        }
    }


    // ============================================================
    // 9.2 — File Churn
    // ============================================================

    public List<FileChurn> analyzeFileChurn(
            Path repositoryPath) {

        try (Git git = Git.open(repositoryPath.toFile())) {

            Map<String, Integer> changeCounts =
                    new HashMap<>();

            Iterable<RevCommit> commits =
                    git.log().call();

            for (RevCommit commit : commits) {

                // Root commit has no parent.
                // There is no previous version to compare against.
                if (commit.getParentCount() == 0) {
                    continue;
                }

                // Use first parent.
                // This keeps merge-commit handling consistent.
                RevCommit parent = commit.getParent(0);

                ObjectId oldTree =
                        parent.getTree().getId();

                ObjectId newTree =
                        commit.getTree().getId();

                List<DiffEntry> changes = git.diff()
                        .setOldTree(
                                new org.eclipse.jgit.treewalk.CanonicalTreeParser(
                                        null,
                                        git.getRepository().newObjectReader(),
                                        oldTree
                                )
                        )
                        .setNewTree(
                                new org.eclipse.jgit.treewalk.CanonicalTreeParser(
                                        null,
                                        git.getRepository().newObjectReader(),
                                        newTree
                                )
                        )
                        .call();

                for (DiffEntry change : changes) {

                    String filePath =
                            change.getNewPath();

                    // Deleted file
                    if (DiffEntry.DEV_NULL.equals(filePath)) {
                        filePath = change.getOldPath();
                    }

                    if (shouldIgnoreFile(filePath)) {
                        continue;
                    }

                    changeCounts.merge(
                            filePath,
                            1,
                            Integer::sum
                    );
                }
            }

            List<FileChurn> result =
                    new ArrayList<>();

            for (Map.Entry<String, Integer> entry :
                    changeCounts.entrySet()) {

                result.add(
                        new FileChurn(
                                entry.getKey(),
                                entry.getValue()
                        )
                );
            }

            result.sort(
                    Comparator.comparingInt(
                                    FileChurn::changeCount
                            )
                            .reversed()
            );

            return result;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to analyze file churn",
                    e
            );
        }
    }


    // ============================================================
    // Common Git file filter
    // ============================================================

    private boolean shouldIgnoreFile(String filePath) {

        return filePath.startsWith("target/")
                || filePath.startsWith(".idea/");
    }


    // ============================================================
    // 9.3 — Contributors
    // ============================================================

    public List<ContributorAnalysis> analyzeContributors(
            Path repositoryPath) {

        try (Git git = Git.open(repositoryPath.toFile())) {

            /*
             * Key = contributor email
             *
             * We intentionally use email as the identity because
             * the same contributor may appear under different names.
             */
            Map<String, Integer> commitCounts =
                    new HashMap<>();

            Map<String, String> contributorNames =
                    new HashMap<>();

            Iterable<RevCommit> commits =
                    git.log().call();

            for (RevCommit commit : commits) {

                PersonIdent author =
                        commit.getAuthorIdent();

                String name =
                        author.getName();

                String email =
                        author.getEmailAddress();

                // Email is used as the stable contributor identity.
                commitCounts.merge(
                        email,
                        1,
                        Integer::sum
                );

                // Keep the latest encountered name.
                contributorNames.put(
                        email,
                        name
                );
            }

            List<ContributorAnalysis> result =
                    new ArrayList<>();

            for (Map.Entry<String, Integer> entry :
                    commitCounts.entrySet()) {

                String email =
                        entry.getKey();

                String name =
                        contributorNames.get(email);

                result.add(
                        new ContributorAnalysis(
                                name,
                                email,
                                entry.getValue()
                        )
                );
            }

            result.sort(
                    Comparator.comparingInt(
                                    ContributorAnalysis::commitCount
                            )
                            .reversed()
            );

            return result;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to analyze contributors",
                    e
            );
        }
    }


    // ============================================================
    // 9.4 — Hotspots
    // ============================================================

    public List<HotspotAnalysis> analyzeHotspots(
            List<FileChurn> fileChurn,
            List<JavaFileAnalysis> javaFiles) {

        List<HotspotAnalysis> hotspots =
                new ArrayList<>();

        /*
         * Map Java analysis results by file path
         * so Git churn data can be combined with
         * static-analysis data.
         */
        Map<String, JavaFileAnalysis> filesByPath =
                new HashMap<>();

        for (JavaFileAnalysis javaFile : javaFiles) {

            filesByPath.put(
                    javaFile.file(),
                    javaFile
            );
        }

        for (FileChurn churn : fileChurn) {

            JavaFileAnalysis javaFile =
                    filesByPath.get(churn.file());

            /*
             * Git may contain files that are not Java files.
             * Those files do not have static-analysis metrics.
             */
            if (javaFile == null) {
                continue;
            }

            // Total complexity of all methods in the file.
            int complexity =
                    javaFile.methods()
                            .stream()
                            .mapToInt(
                                    JavaMethodAnalysis::complexity
                            )
                            .sum();

            // Total dependencies of all classes in the file.
            int dependencyCount =
                    javaFile.classMetrics()
                            .stream()
                            .mapToInt(
                                    JavaClassAnalysis::dependencyCount
                            )
                            .sum();

            // Number of detected code smells.
            int codeSmellCount =
                    javaFile.codeSmells().size();

            hotspots.add(
                    new HotspotAnalysis(
                            javaFile.file(),
                            churn.changeCount(),
                            javaFile.loc(),
                            complexity,
                            codeSmellCount,
                            dependencyCount
                    )
            );
        }

        /*
         * TEMPORARY hotspot ordering.
         *
         * This is NOT the final risk formula.
         * Phase 10 will replace this with normalized,
         * weighted risk calculations.
         */
        hotspots.sort(
                Comparator.comparingInt(
                                this::calculateHotspotScore
                        )
                        .reversed()
        );

        return hotspots;
    }


    /*
     * Temporary Phase 9 hotspot score.
     *
     * Phase 10 will replace this with a proper
     * normalized risk calculation.
     */
    private int calculateHotspotScore(
            HotspotAnalysis hotspot) {

        return hotspot.changeCount()
                + hotspot.codeSmellCount()
                + hotspot.dependencyCount()
                + hotspot.complexity();
    }


    // ============================================================
    // 9.5 — File Ownership
    // ============================================================

    public List<FileOwnership> analyzeOwnership(
            Path repositoryPath) {

        /*
         * Structure:
         *
         * file
         *   └── author email
         *          └── number of changes
         *
         * Example:
         *
         * Vehicle.java
         *     kishor@gmail.com -> 4
         *     developer@gmail.com -> 1
         */
        Map<String, Map<String, Integer>> ownershipMap =
                new HashMap<>();

        /*
         * Stores a display name for each email.
         *
         * Email is the identity.
         * Name is only presentation information.
         */
        Map<String, String> contributorNames =
                new HashMap<>();

        try (Git git = Git.open(repositoryPath.toFile())) {

            /*
             * Use the same commit traversal as the other
             * Git-history analyses.
             */
            Iterable<RevCommit> commits =
                    git.log().call();

            for (RevCommit commit : commits) {

                PersonIdent author =
                        commit.getAuthorIdent();

                String authorName =
                        author.getName();

                String authorEmail =
                        author.getEmailAddress();

                /*
                 * Email is the stable contributor identity.
                 *
                 * This handles:
                 *
                 * kishor <same@email.com>
                 * Kishor V <same@email.com>
                 *
                 * as one contributor.
                 */
                contributorNames.put(
                        authorEmail,
                        authorName
                );

                /*
                 * Root commit has no parent.
                 *
                 * For the current Phase 9 definition,
                 * ownership is based on changes between
                 * a commit and its first parent.
                 *
                 * Therefore the root commit is skipped,
                 * just like file churn.
                 */
                if (commit.getParentCount() == 0) {
                    continue;
                }

                /*
                 * Use first parent for consistency with
                 * analyzeFileChurn().
                 */
                RevCommit parent =
                        commit.getParent(0);

                ObjectId oldTree =
                        parent.getTree().getId();

                ObjectId newTree =
                        commit.getTree().getId();

                List<DiffEntry> changes =
                        git.diff()
                                .setOldTree(
                                        new org.eclipse.jgit.treewalk.CanonicalTreeParser(
                                                null,
                                                git.getRepository()
                                                        .newObjectReader(),
                                                oldTree
                                        )
                                )
                                .setNewTree(
                                        new org.eclipse.jgit.treewalk.CanonicalTreeParser(
                                                null,
                                                git.getRepository()
                                                        .newObjectReader(),
                                                newTree
                                        )
                                )
                                .call();

                for (DiffEntry change : changes) {

                    String filePath =
                            change.getNewPath();

                    /*
                     * Deleted files don't have a new path.
                     */
                    if (DiffEntry.DEV_NULL.equals(filePath)) {
                        filePath = change.getOldPath();
                    }

                    if (shouldIgnoreFile(filePath)) {
                        continue;
                    }

                    /*
                     * Count one ownership change for
                     * the author of this commit.
                     */
                    ownershipMap
                            .computeIfAbsent(
                                    filePath,
                                    key -> new HashMap<>()
                            )
                            .merge(
                                    authorEmail,
                                    1,
                                    Integer::sum
                            );
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to analyze repository ownership",
                    e
            );
        }


        // --------------------------------------------------------
        // Convert raw ownership map into FileOwnership DTOs
        // --------------------------------------------------------

        List<FileOwnership> ownership =
                new ArrayList<>();

        for (Map.Entry<String, Map<String, Integer>> entry :
                ownershipMap.entrySet()) {

            String file =
                    entry.getKey();

            Map<String, Integer> authors =
                    entry.getValue();

            String ownerEmail = null;
            int ownerChangeCount = 0;

            /*
             * Find the contributor with the highest
             * number of changes to this file.
             */
            for (Map.Entry<String, Integer> authorEntry :
                    authors.entrySet()) {

                if (authorEntry.getValue()
                        > ownerChangeCount) {

                    ownerEmail =
                            authorEntry.getKey();

                    ownerChangeCount =
                            authorEntry.getValue();
                }
            }

            /*
             * Total number of commits that modified
             * this file.
             */
            int totalChanges =
                    authors.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum();

            /*
             * Percentage of file changes attributed
             * to the dominant contributor.
             */
            double ownershipPercentage =
                    totalChanges == 0
                            ? 0.0
                            : (ownerChangeCount * 100.0)
                            / totalChanges;

            String ownerName =
                    contributorNames.get(ownerEmail);

            ownership.add(
                    new FileOwnership(
                            file,
                            ownerName,
                            ownerEmail,
                            ownerChangeCount,
                            totalChanges,
                            ownershipPercentage
                    )
            );
        }

        /*
         * Files with the highest amount of historical
         * activity appear first.
         */
        ownership.sort(
                Comparator.comparingInt(
                                FileOwnership::totalChanges
                        )
                        .reversed()
        );

        return ownership;
    }
}