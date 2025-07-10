package io.github.zorin95670.semver.service;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;

public class MavenService {

    private final MavenProject project;

    public MavenService(MavenProject project) {
        this.project = project;
    }

    public void upgradeVersion(String version, String tagPrefix) {
        File pomFile = project.getFile(); // usually pom.xml

        try (FileReader reader = new FileReader(pomFile)) {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);

            model.setVersion(version.substring(tagPrefix.length()));

            try (FileWriter writer = new FileWriter(pomFile)) {
                MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
                xpp3Writer.write(writer, model);
            }

        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }
}
