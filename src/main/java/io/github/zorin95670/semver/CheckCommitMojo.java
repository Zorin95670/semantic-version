package io.github.zorin95670.semver;

import io.github.zorin95670.semver.service.GitService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "check-commit", defaultPhase = LifecyclePhase.NONE)
public class CheckCommitMojo extends AbstractMojo {

    @Parameter(property = "failOnWarning", defaultValue = "false")
    private boolean failOnWarning;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    public void execute() throws MojoFailureException {
        getLog().info("Check commit plugin started");
        GitService gitService = new GitService(basedir);

        gitService.checkCommitNames(failOnWarning, getLog());
    }
}
