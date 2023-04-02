package rife.bld.extension.propertyFile;

import rife.bld.Project;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishInfo;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;

import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;

public class PropertyFileBuild extends Project {
    public PropertyFileBuild() {
        pkg = "rife.bld.extension";
        name = "bld-property-file";
        version = version(0, 9, 0);
        javadocOptions.add("-Xdoclint:-missing");
        publishRepository = MAVEN_LOCAL;
        publishInfo = new PublishInfo().groupId("com.uwyn.rife2").artifactId("bld-property-file")
                .description("Bld Extension to Edit or Create Properties Files")
                .url("https://github.com/rife2/bld-property-file")
                .developer(new PublishDeveloper().id("ethauvin").name("Erik C. Thauvin").email("erik@thauvin.net")
                        .url("https://erik.thauvin.net/"))
                .license(new PublishLicense().name("The Apache License, Version 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
                .scm(new PublishScm().connection("scm:git:https://github.com/rife2/bld-property-file.git")
                        .developerConnection("scm:git:git@github.com:rife2/bld-property-file.git")
                        .url("https://github.com/rife2/bld-property-file"));

        javaRelease = 17;

        downloadSources = true;
        repositories = List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS);
        scope(compile)
                .include(dependency("com.uwyn.rife2", "rife2", version(1, 5, 11)));
        scope(test)
                .include(dependency("org.jsoup", "jsoup", version(1, 15, 4)))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 9, 2)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 9, 2)))
                .include(dependency("org.assertj:assertj-joda-time:2.2.0"));

    }

    public static void main(String[] args) {
        new PropertyFileBuild().start(args);
    }
}