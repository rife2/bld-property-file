package rife.bld.extension.propertyfile;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.JacocoReportOperation;
import rife.bld.extension.PmdOperation;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;

import java.io.IOException;
import java.util.List;

import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Repository.RIFE2_RELEASES;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;
import static rife.bld.operations.JavadocOptions.DocLinkOption.NO_MISSING;

public class PropertyFileBuild extends Project {
    public PropertyFileBuild() {
        pkg = "rife.bld.extension";
        name = "bld-property-file";
        version = version(0, 9, 2, "SNAPSHOT");

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);

        var rife2 = version(1,7,0);
        scope(compile)
                .include(dependency("com.uwyn.rife2", "rife2", rife2))
                .include(dependency("com.uwyn.rife2", "bld", rife2));
        scope(test)
                .include(dependency("org.jsoup", "jsoup", version(1, 16, 1)))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 10, 0)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 10, 0)))
                .include(dependency("org.assertj:assertj-joda-time:2.2.0"));

        javadocOperation()
                .javadocOptions()
                .docLint(NO_MISSING)
                .link("https://rife2.github.io/bld/")
                .link("https://rife2.github.io/rife2/");

        publishOperation()
                .repository(version.isSnapshot() ? repository("rife2-snapshot") : repository("rife2"))
                .info()
                .groupId("com.uwyn.rife2")
                .artifactId("bld-property-file")
                .description("bld Extension to Create or Modify Properties Files")
                .url("https://github.com/rife2/bld-property-file")
                .developer(new PublishDeveloper().id("ethauvin").name("Erik C. Thauvin").email("erik@thauvin.net")
                        .url("https://erik.thauvin.net/"))
                .developer(new PublishDeveloper().id("gbevin").name("Geert Bevin").email("gbevin@uwyn.com")
                        .url("https://github.com/gbevin"))
                .license(new PublishLicense().name("The Apache License, Version 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
                .scm(new PublishScm().connection("scm:git:https://github.com/rife2/bld-property-file.git")
                        .developerConnection("scm:git:git@github.com:rife2/bld-property-file.git")
                        .url("https://github.com/rife2/bld-property-file"))
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase"));
    }

    public static void main(String[] args) {
        new PropertyFileBuild().start(args);
    }

    @BuildCommand(summary = "Generates JaCoCo Reports")
    public void jacoco() throws IOException {
        new JacocoReportOperation()
                .fromProject(this)
                .execute();
    }

    @BuildCommand(summary = "Runs PMD analysis")
    public void pmd() throws Exception {
        new PmdOperation()
                .fromProject(this)
                .failOnViolation(true)
                .ruleSets("config/pmd.xml")
                .execute();
    }
}
