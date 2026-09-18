package com.repopulse.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import com.repopulse.dto.CommitInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitService {

    public String cloneRepository(
            String cloneUrl,
            String localPath) throws Exception {

        File directory = new File(localPath);

        Git.cloneRepository()
                .setURI(cloneUrl)
                .setDirectory(directory)
                .call();

        return directory.getAbsolutePath();
    }
    public List<String> readBranches(String localPath) throws Exception {

        try (Git git = Git.open(new File(localPath))) {

            return git.branchList()
                    .call()
                    .stream()
                    .map(ref -> ref.getName())
                    .toList();
        }
    }
    public List<CommitInfo> readCommits(
            String localPath) throws Exception {

        try (Git git = Git.open(new File(localPath))) {

            Iterable<RevCommit> commits =
                    git.log().call();

            List<CommitInfo> result =
                    new ArrayList<>();

            for (RevCommit commit : commits) {

                result.add(
                        new CommitInfo(
                                commit.name(),
                                commit.getAuthorIdent().getName(),
                                commit.getShortMessage(),
                                commit.getAuthorIdent()
                                        .getWhen()
                                        .toInstant()
                        )
                );
            }

            return result;
        }
    }

    public List<String> readDiff(
            String localPath,
            String oldCommitId,
            String newCommitId) throws Exception {

        try (Git git = Git.open(new File(localPath))) {

            Repository repository = git.getRepository();

            ObjectId oldCommit =
                    repository.resolve(oldCommitId);

            ObjectId newCommit =
                    repository.resolve(newCommitId);

            try (RevWalk walk = new RevWalk(repository)) {

                RevCommit oldCommitObject =
                        walk.parseCommit(oldCommit);

                RevCommit newCommitObject =
                        walk.parseCommit(newCommit);

                CanonicalTreeParser oldTree =
                        new CanonicalTreeParser();

                oldTree.reset(
                        repository.newObjectReader(),
                        oldCommitObject.getTree());

                CanonicalTreeParser newTree =
                        new CanonicalTreeParser();

                newTree.reset(
                        repository.newObjectReader(),
                        newCommitObject.getTree());

                try (DiffFormatter formatter =
                             new DiffFormatter(
                                     DisabledOutputStream.INSTANCE)) {

                    formatter.setRepository(repository);

                    List<DiffEntry> diffs =
                            formatter.scan(
                                    oldTree,
                                    newTree);

                    List<String> result =
                            new ArrayList<>();

                    for (DiffEntry diff : diffs) {

                        result.add(
                                diff.getChangeType()
                                        + " : "
                                        + diff.getNewPath()
                        );
                    }

                    return result;
                }
            }
        }
    }
}