package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.propertyFile.Entry;
import rife.bld.extension.propertyFile.Entry.Operations;
import rife.bld.extension.propertyFile.Entry.Types;
import rife.bld.extension.propertyFile.PropertyFileOperation;

import java.util.List;

import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Repository.SONATYPE_SNAPSHOTS;
import static rife.bld.dependencies.Scope.test;

public class PropertyFileExampleBuild extends Project {

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

    @BuildCommand
    public void updateMajor() throws Exception {
        new PropertyFileOperation(this)
                .file("version.properties")
                .entry(new Entry("version.major").defaultValue(1).type(Types.INT).operation(Operations.ADD))
                .entry(new Entry("version.minor").value(0))
                .entry(new Entry("version.patch").value(0))
                .execute();
    }

    @BuildCommand
    public void updateMinor() throws Exception {
        new PropertyFileOperation(this)
                .file("version.properties")
                .entry(new Entry("version.minor").defaultValue(0).type(Types.INT).operation(Operations.ADD))
                .entry(new Entry("version.patch").value(0))
                .execute();
    }

    @BuildCommand
    public void updatePatch() throws Exception {
        new PropertyFileOperation(this)
                .file("version.properties")
                .entry(new Entry("version.patch").defaultValue(0).type(Types.INT).operation(Operations.ADD))
                .execute();
    }
}