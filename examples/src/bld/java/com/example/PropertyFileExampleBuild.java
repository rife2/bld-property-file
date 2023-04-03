package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.propertyfile.Entry;
import rife.bld.extension.propertyfile.Entry.Operations;
import rife.bld.extension.propertyfile.Entry.Types;
import rife.bld.extension.propertyfile.PropertyFileOperation;

import java.util.List;

import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Repository.SONATYPE_SNAPSHOTS;
import static rife.bld.dependencies.Scope.test;

public class PropertyFileExampleBuild extends Project {
    final Entry buildDateEntry = new Entry("build.date").value("now").pattern("yyyy-MM-dd").type(Types.DATE);

    public PropertyFileExampleBuild() {
        pkg = "com.example";
        name = "PropertyFileExample";
        mainClass = "com.example.PropertyFileExampleMain";
        version = version(0, 1, 0);

        downloadSources = true;
        repositories = List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS);
        scope(test)
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 9, 2)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 9, 2)));

    }

    public static void main(String[] args) {
        new PropertyFileExampleBuild().start(args);
    }

    @BuildCommand(summary = "Updates major version")
    public void updateMajor() throws Exception {
        new PropertyFileOperation(this)
                .file("version.properties")
                // set the major version to 1 if it doesn't exist, increase by 1
                .entry(new Entry("version.major").defaultValue(0).type(Types.INT).operation(Operations.ADD))
                // set the minor version to 0
                .entry(new Entry("version.minor").value(0))
                // set the patch version to 0
                .entry(new Entry("version.patch").value(0))
                // set the build date to the current date
                .entry(buildDateEntry)
                .execute();
    }

    @BuildCommand(summary = "Updates minor version")
    public void updateMinor() throws Exception {
        new PropertyFileOperation(this)
                .file("version.properties")
                // set the major version to 1 if it doesn't exist
                .entry(new Entry("version.major").defaultValue(1))
                // set the minor version to 0 if it doesn't exist, increase by 1
                .entry(new Entry("version.minor").defaultValue(-1).type(Types.INT).operation(Operations.ADD))
                // set the patch version to 0
                .entry(new Entry("version.patch").value(0))
                // set the build date to the current date
                .entry(buildDateEntry)
                .execute();
    }

    @BuildCommand(summary = "Updates patch version")
    public void updatePatch() throws Exception {
        new PropertyFileOperation(this)
                .file("version.properties")
                // set the major version to 1 if it doesn't exist
                .entry(new Entry("version.major").defaultValue(1))
                // set the minor version to 0 if it doesn't exist
                .entry(new Entry("version.minor").defaultValue(0))
                // set the patch version to 10 if it doesn't exist, increase by 10
                .entry(new Entry("version.patch").defaultValue(0).type(Types.INT).operation(Operations.ADD).value(10))
                // set the build date to the current date
                .entry(buildDateEntry)
                .execute();
    }
}