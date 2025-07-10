package io.github.zorin95670.semver.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ChangelogReleaseSection {
    public List<Section> SECTIONS_ORDER = List.of(Section.Added, Section.Fixed, Section.Changed, Section.Removed, Section.Security, Section.Deprecated);
    public boolean isUnreleased = true;
    public HashMap<Section, List<String>> sections = new HashMap<>();
    private String title;
    public String version;

    public void setTitle(String version, Instant date, String tagPrefix) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());
        String formattedDate = formatter.format(date);
        title = String.format("[%s] - %s", version.substring(tagPrefix.length()), formattedDate);
        this.version = version;
        isUnreleased = false;
    }

    public String getTitle() {
        if (title == null) {
            return "[Unreleased]";
        }

        return title;
    }

    public List<String> getSectionNames() {
        return SECTIONS_ORDER.stream()
            .filter(sections::containsKey)
            .map(Section::name)
            .collect(Collectors.toList());
    }

    public List<String> getCommits(String section) {
        return sections.get(Section.valueOf(section));
    }

    public void add(RevCommit commit) {
        String message = commit.getShortMessage();
        Section section = getSection(message);

        if (section == null) {
            return;
        }

        sections
            .computeIfAbsent(section, k -> new ArrayList<>())
            .add(message.substring(message.indexOf(':') + 1).trim());
    }

    private Section getSection(String message) {
        String lower = message.toLowerCase();

        return Arrays.stream(Section.values())
            .filter(section -> section.prefixes.stream().anyMatch(lower::startsWith))
            .findFirst()
            .orElse(null);
    }

    public enum Section {
        Added("feat"),
        Fixed("fix"),
        Changed("perf", "refactor", "style", "ci", "build"),
        Removed("removed"),
        Security("security"),
        Deprecated("deprecated");

        public final List<String> prefixes;

        Section(String... prefixes) {
            this.prefixes = Arrays.asList(prefixes);
        }
    }
}
