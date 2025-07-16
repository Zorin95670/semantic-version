package io.github.zorin95670.semver.service;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.eclipse.jgit.lib.Constants.R_TAGS;

public class GitService {

    private Repository repository;
    private Git git;

    public GitService(File projectDir) {
        initRepository(projectDir);
    }

    public void initRepository(File projectDir) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder
                .setWorkTree(projectDir)
                .readEnvironment()
                .findGitDir()
                .build();

            git = Git.wrap(repository);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, List<String>> getAllTags(RevTag origin, String tagPrefix) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit headCommit = revWalk.parseCommit(repository.resolve("HEAD"));

            RevCommit originCommit = null;
            if (origin != null) {
                originCommit = revWalk.parseCommit(origin.getObject());
            }

            // Expression régulière SemVer : X.Y.Z
            Pattern semverPattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

            List<Ref> tagRefs = repository.getRefDatabase().getRefsByPrefix(R_TAGS);
            Map<ObjectId, List<String>> tagMap = new HashMap<>();

            for (Ref ref : tagRefs) {
                String tagName = Repository.shortenRefName(ref.getName());

                if (!tagName.startsWith(tagPrefix)) {
                    continue;
                }

                // On filtre les tags non SemVer
                if (!semverPattern.matcher(tagName.substring(tagPrefix.length())).matches()) {
                    continue;
                }

                try {
                    RevObject object = revWalk.parseAny(ref.getObjectId());

                    if (object instanceof RevTag tag) {
                        RevObject peeled = revWalk.peel(tag);
                        if (peeled instanceof RevCommit commit) {
                            tagMap.computeIfAbsent(commit.getId(), id -> new ArrayList<>())
                                .add(tagName);
                        }
                    }

                } catch (IOException ignored) {
                    // On ignore les erreurs de parsing
                }
            }

            // Parcours depuis HEAD
            revWalk.markStart(headCommit);
            Map<String, List<String>> result = new LinkedHashMap<>();

            for (RevCommit commit : revWalk) {
                if (originCommit != null && commit.equals(originCommit)) {
                    break;
                }

                List<String> tags = tagMap.get(commit.getId());
                if (tags != null && !tags.isEmpty()) {
                    result.put(commit.getId().getName(), tags);
                }
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve annotated tags", e);
        }
    }


    public Optional<RevTag> getLastTag(String tagPrefix) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit headCommit = revWalk.parseCommit(repository.resolve("HEAD"));
            List<Ref> tagRefs = repository.getRefDatabase().getRefsByPrefix(R_TAGS);

            return tagRefs.stream()
                .map(ref -> {
                    try {
                        RevObject object = revWalk.parseAny(ref.getObjectId());

                        if (object instanceof RevTag) {
                            RevTag tag = (RevTag) object;
                            RevObject target = revWalk.parseAny(tag.getObject());

                            if (target instanceof RevCommit) {
                                RevCommit targetCommit = (RevCommit) target;
                                if (targetCommit.getCommitTime() <= headCommit.getCommitTime()) {
                                    return tag;
                                }
                            }
                        }
                        return null;
                    } catch (IOException e) {
                        throw new RuntimeException("Erreur avec le tag : " + ref.getName(), e);
                    }
                })
                .filter(Objects::nonNull)
                .filter(tagRef -> tagRef.getTagName().startsWith(tagPrefix))
                .max(Comparator.comparingInt(tag -> {
                    try {
                        RevCommit targetCommit = revWalk.parseCommit(tag.getObject());
                        return targetCommit.getCommitTime();
                    } catch (IOException e) {
                        return Integer.MIN_VALUE;
                    }
                }));
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la récupération du dernier tag", e);
        }
    }

    public List<RevCommit> getCommitsFrom(RevTag origin, String scope) {
        List<RevCommit> commits = new ArrayList<>();
        var scopePattern = Pattern.compile("^\\w+\\(" + Pattern.quote(scope) + "\\):");

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit head = revWalk.parseCommit(repository.resolve("HEAD"));
            revWalk.markStart(head);

            RevCommit stopCommit = null;
            if (origin != null) {
                RevObject target = revWalk.parseAny(origin.getObject());
                if (target instanceof RevCommit) {
                    stopCommit = (RevCommit) target;
                }
            }

            for (RevCommit commit : revWalk) {
                if (stopCommit != null && commit.equals(stopCommit)) {
                    break;
                }

                if (scope.isEmpty() || scopePattern.matcher(commit.getShortMessage()).matches()) {
                    commits.add(commit);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la récupération des commits depuis HEAD", e);
        }

        return commits;
    }

    public Optional<String> getNewTagName(List<RevCommit> commits, RevTag lastTag, String tagPrefix) {
        int major = 0, minor = 0, patch = 0;

        // 1. Extraire version depuis le dernier tag (format vX.Y.Z)
        if (lastTag != null) {
            String tagName = lastTag.getTagName().trim();
            if (tagName.startsWith(tagPrefix)) {
                tagName = tagName.substring(tagPrefix.length());
            }
            String[] parts = tagName.split("\\.");
            if (parts.length >= 3) {
                try {
                    major = Integer.parseInt(parts[0]);
                    minor = Integer.parseInt(parts[1]);
                    patch = Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 2. Flags d'incrément
        boolean incrementMajor = false;
        boolean incrementMinor = false;
        boolean incrementPatch = false;

        for (RevCommit commit : commits) {
            String fullMsg = commit.getFullMessage();

            // 2.1 Check BREAKING CHANGE (dans le corps ou footer)
            if (fullMsg.contains("BREAKING CHANGE") || fullMsg.contains("BREAKING-CHANGE")) {
                incrementMajor = true;
                break; // priorité max, on peut arrêter
            }

            // 2.2 Parse la première ligne (header) du commit
            String header = fullMsg.split("\n", 2)[0];

            // Exemple header : feat!: message, fix(scope): message
            // Regex pour extraire type et "!" :
            // ^(\w+)(!)?(\(.+\))?: .+
            Matcher matcher = Pattern.compile("^(\\w+)(!)?(\\(.+\\))?:").matcher(header);

            if (matcher.find()) {
                String type = matcher.group(1);
                boolean breakingExclamation = matcher.group(2) != null;

                if (breakingExclamation) {
                    incrementMajor = true;
                    break;
                }

                if ("feat".equals(type)) {
                    incrementMinor = true;
                } else if ("fix".equals(type)) {
                    incrementPatch = true;
                }
                // on ignore les autres types
            }
        }

        boolean changed = true;

        // 3. Appliquer incrément en priorité : MAJOR > MINOR > PATCH
        if (incrementMajor) {
            major++;
            minor = 0;
            patch = 0;
        } else if (incrementMinor) {
            minor++;
            patch = 0;
        } else if (incrementPatch) {
            patch++;
        } else if (!commits.isEmpty()) {
            patch++; // commits sans impact → patch++
        } else {
            return Optional.empty();
        }

        return Optional.of(String.format("%s%d.%d.%d", tagPrefix, major, minor, patch));
    }

    public void add(String pattern) {
        try {
            git.add().addFilepattern(pattern).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit(String message) {
        try {
            git.commit().setMessage(message).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void amend() {
        try {
            ObjectId headId = git.getRepository().resolve("HEAD");

            try (RevWalk revWalk = new RevWalk(git.getRepository())) {
                RevCommit lastCommit = revWalk.parseCommit(headId);

                git.commit()
                    .setAmend(true)
                    .setMessage(lastCommit.getFullMessage())
                    .setAuthor(lastCommit.getAuthorIdent())
                    .setCommitter(new PersonIdent(git.getRepository()))
                    .call();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void tag(String version) {
        try {
            git.tag()
                .setName(version)
                .setMessage("Release " + version)
                .setAnnotated(true)
                .setForceUpdate(true)
                .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUrl() {
        List<RemoteConfig> remotes = null;
        try {
            remotes = RemoteConfig.getAllRemoteConfigs(git.getRepository().getConfig());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        for (RemoteConfig remote : remotes) {
            if ("origin".equals(remote.getName())) {
                for (URIish uri : remote.getURIs()) {
                    return uri.toString();
                }
            }
        }

        return null;
    }

    public void checkCommitNames(boolean failOnWarning, Log log, boolean noMerge) throws MojoFailureException {
        List<String> validPrefixes = Arrays.asList(
            "feat", "fix", "perf", "refactor", "style", "ci", "build",
            "removed", "security", "deprecated"
        );

        Repository repo = git.getRepository();

        Iterable<RevCommit> commits;

        try {
            List<Ref> tagList = git.tagList().call();
            RevCommit lastTaggedCommit = null;

            if (!tagList.isEmpty()) {
                RevWalk revWalk = new RevWalk(repo);
                RevCommit latest = null;
                for (Ref tag : tagList) {
                    RevCommit commit = revWalk.parseCommit(repo.resolve(tag.getName()));
                    if (latest == null || commit.getCommitTime() > latest.getCommitTime()) {
                        latest = commit;
                    }
                }
                lastTaggedCommit = latest;
                revWalk.dispose();
            }

            if (lastTaggedCommit != null) {
                commits = git.log().add(repo.resolve("HEAD")).not(lastTaggedCommit).call();
            } else {
                commits = git.log().call();
            }
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }

        boolean hasError = false;
        boolean hasWarning = false;

        for (RevCommit commit : commits) {
            String message = commit.getShortMessage();

            if (commit.getParentCount() > 1) {
                log.info("Skipping merge commit: \"" + message + "\"");
                continue;
            }

            if (!message.matches("^[a-z]+(\\([\\w\\-]+\\))?: .+")) {
                log.error("Invalid commit format: \"" + message + "\"");
                hasError = true;
                continue;
            }

            String type = message.split("[:(]")[0];
            if (!validPrefixes.contains(type)) {
                log.warn("Unsupported commit prefix: \"" + message + "\"");
                hasWarning = true;
            }
        }

        if (hasError || (failOnWarning && hasWarning)) {
            throw new MojoFailureException("Some commits do not follow the Conventional Commit format.");
        } else {
            log.info("✅ all commits are valid.");
        }
    }
}
