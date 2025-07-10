package io.github.zorin95670.semver;


import io.github.zorin95670.semver.service.ChangelogService;
import io.github.zorin95670.semver.service.GitService;
import io.github.zorin95670.semver.service.MavenService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

@Mojo(name = "release", defaultPhase = LifecyclePhase.NONE)
public class ReleaseMojo extends AbstractMojo {

    @Parameter(property = "tagPrefix", defaultValue = "v")
    private String tagPrefix;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    public void execute() throws MojoExecutionException {
        MavenService mavenService = new MavenService(project);
        GitService gitService = new GitService(basedir);
        ChangelogService changelogService = new ChangelogService(basedir);

        var lastTag = gitService.getLastTag(tagPrefix).orElse(null);
        var commits = gitService.getCommitsFrom(lastTag);
        var opt = gitService.getNewTagName(commits, lastTag, tagPrefix);

        if (opt.isEmpty()) {
            getLog().info("No new tag found");
            return;
        }

        var nextTag = opt.get();

        mavenService.upgradeVersion(nextTag, tagPrefix);
        gitService.add("pom.xml");
        gitService.commit(String.format("chore: bump to version %s [ci skip]", nextTag));
        gitService.tag(nextTag);
        changelogService.generateFromBeginning(
            gitService.getUrl(),
            gitService.getCommitsFrom(null),
            gitService.getAllTags(null, tagPrefix),
            false,
            tagPrefix
        );
        gitService.add("changelog.md");
        gitService.amend();
        gitService.tag(nextTag);
    }
}
