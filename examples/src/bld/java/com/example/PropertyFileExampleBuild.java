package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.propertyfile.Entry;
import rife.bld.extension.propertyfile.EntryDate;
import rife.bld.extension.propertyfile.EntryInt;
import rife.bld.extension.propertyfile.PropertyFileOperation;

import java.util.List;

import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Repository.RIFE2_RELEASES;
import static rife.bld.dependencies.Scope.test;
import static rife.bld.extension.propertyfile.Calc.ADD;

public class PropertyFileExampleBuild extends Project {
    final EntryDate buildDateEntry = new EntryDate("build.date").now().pattern("yyyy-MM-dd");

    public PropertyFileExampleBuild() {
        pkg = "com.example";
        name = "PropertyFileExample";
        mainClass = "com.example.PropertyFileExampleMain";
        version = version(0, 1, 0);

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);

        scope(test)
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 9, 3)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 9, 3)));
    }

    public static void main(String[] args) {
        new PropertyFileExampleBuild().start(args);
    }

    @BuildCommand(summary = "Updates major version")
    public void updateMajor() throws Exception {
        new PropertyFileOperation()
                .fromProject(this)
                .file("version.properties")
                // set the major version to 1 if it doesn't exist, increase by 1
                .entry(new EntryInt("version.major").defaultValue(0).calc(ADD))
                // set the minor version to 0
                .entry(new EntryInt("version.minor").set(0))
                // set the patch version to 0
                .entry(new EntryInt("version.patch").set(0))
                // set the build date to the current date
                .entry(buildDateEntry)
                .execute();
    }

    @BuildCommand(summary = "Updates minor version")
    public void updateMinor() throws Exception {
        new PropertyFileOperation()
                .fromProject(this)
                .file("version.properties")
                // set the major version to 1 if it doesn't exist
                .entry(new EntryInt("version.major").defaultValue(1))
                // set the minor version to 0 if it doesn't exist, increase by 1
                .entry(new EntryInt("version.minor").defaultValue(-1).calc(ADD))
                // set the patch version to 0
                .entry(new EntryInt("version.patch").set(0))
                // set the build date to the current date
                .entry(buildDateEntry)
                .execute();
    }

    @BuildCommand(summary = "Updates patch version")
    public void updatePatch() throws Exception {
        new PropertyFileOperation()
                .fromProject(this)
                .file("version.properties")
                // set the major version to 1 if it doesn't exist
                .entry(new EntryInt("version.major").defaultValue(1))
                // set the minor version to 0 if it doesn't exist
                .entry(new EntryInt("version.minor").defaultValue(0))
                // set the patch version to 10 if it doesn't exist, increase by 10
                .entry(new EntryInt("version.patch").defaultValue(0).calc(v -> v + 10))
                // set the build date to the current date
                .entry(buildDateEntry)
                .execute();
    }

    @BuildCommand(summary = "Updates the release")
    public void updateRelease() throws Exception {
        new PropertyFileOperation()
                .fromProject(this)
                .file("version.properties")
                // set the release to current date/time
                .entry(new EntryDate("release").now().pattern("yyyyMMddHHmmss"))
                // prepend 'beta.' to the release
                .entry(new Entry("release").modify("beta.", (v, s) -> s + v))
                .execute();
    }

    @BuildCommand(summary = "Delete version properties")
    public void deleteVersion() throws Exception {
        new PropertyFileOperation()
                .fromProject(this)
                .file("version.properties")
                .entry(new Entry("version.major").delete())
                .entry(new Entry("version.minor").delete())
                .entry(new Entry("version.patch").delete())
                .entry(new Entry("release").delete())
                .entry(buildDateEntry.delete())
                .execute();
    }
}