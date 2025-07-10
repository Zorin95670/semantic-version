package io.github.zorin95670.semver;

import io.github.zorin95670.semver.service.ChangelogService;
import io.github.zorin95670.semver.service.GitService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "changelog", defaultPhase = LifecyclePhase.NONE)
public class GenerateChangelogMojo extends AbstractMojo {

    @Parameter(property = "tagPrefix", defaultValue = "v")
    private String tagPrefix;

    @Parameter(property = "dryRun", defaultValue = "false")
    private boolean dryRun;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    public void execute() throws MojoExecutionException {
        getLog().info("Generate Changelog plugin started");
        GitService gitService = new GitService(basedir);
        ChangelogService changelogService = new ChangelogService(basedir);


        changelogService.generateFromBeginning(
            gitService.getUrl(),
            gitService.getCommitsFrom(null),
            gitService.getAllTags(null, tagPrefix),
            dryRun,
            tagPrefix
        );
    }
}
