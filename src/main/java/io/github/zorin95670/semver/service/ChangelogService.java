package io.github.zorin95670.semver.service;

import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import io.github.zorin95670.semver.model.ChangelogReleaseSection;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ChangelogService {
    private final File projectDir;

    public ChangelogService(File projectDir) {
        this.projectDir = projectDir;
    }

    public void generateFromBeginning(String url, List<RevCommit> commits, Map<String, List<String>> taggedCommits, boolean dryRun) {
        List<ChangelogReleaseSection> releaseSections = new ArrayList<>();

        commits.forEach(commit -> {
            var tag = isTaggedCommit(commit, taggedCommits);

            if (releaseSections.isEmpty()) {
                ChangelogReleaseSection currentSection = new ChangelogReleaseSection();

                tag.ifPresent(s -> currentSection.setTitle(s, Instant.ofEpochSecond(commit.getCommitTime())));

                currentSection.add(commit);
                releaseSections.add(currentSection);
                return;
            }

            if (tag.isPresent()) {
                ChangelogReleaseSection currentSection = new ChangelogReleaseSection();
                currentSection.setTitle(tag.get(), Instant.ofEpochSecond(commit.getCommitTime()));
                currentSection.add(commit);
                releaseSections.add(currentSection);
                return;
            }

            releaseSections.getLast().add(commit);
        });

        List<String> versions = new ArrayList<>();
        for (int i = 0; i < releaseSections.size(); i++) {
            if (url == null) {
                break;
            }

            String currentVersion = releaseSections.get(i).version;
            String previousVersion = null;

            if (i + 1 < releaseSections.size()) {
                previousVersion = releaseSections.get(i + 1).version;
            }

            if (currentVersion == null && previousVersion == null) {
                continue;
            }

            if (currentVersion == null) {
                versions.add(String.format("[unreleased]: %s/compare/%s...HEAD", url, previousVersion));
                continue;
            }

            if (previousVersion == null) {
                versions.add(String.format("[%s]: %s/releases/tag/%s", currentVersion.substring(1), url, currentVersion));
                continue;
            }

            versions.add(String.format(
                "[%s]: %s/compare/%s...%s",
                currentVersion.substring(1),
                url,
                previousVersion,
                currentVersion
            ));
        }

        Jinjava jinjava = new Jinjava();
        Map<String, Object> context = Map.of(
            "releases",
            releaseSections.stream()
                .filter(section -> !section.getSectionNames().isEmpty())
                .toList(),
            "versions",
                versions
        );

        String template = null;
        try {
            template = Resources.toString(Resources.getResource("changelog-template.md"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String renderedTemplate = jinjava.render(template, context);

        if (dryRun) {
            System.out.println(renderedTemplate);
            return;
        }

        try (FileWriter fileWriter = new FileWriter(projectDir.getAbsolutePath() + "/changelog.md")) {
            fileWriter.write(renderedTemplate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<String> isTaggedCommit(RevCommit commit, Map<String, List<String>> taggedCommits) {
        String id = commit.getId().getName();

        if (!taggedCommits.containsKey(id)) {
            return Optional.empty();
        }
        List<String> tags = taggedCommits.get(id);

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tags.getFirst());
    }
}
