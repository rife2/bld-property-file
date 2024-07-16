/*
 * Copyright 2023-Copyright $today.yearamp;#36;today.year the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rife.bld.extension.propertyfile;

import org.junit.jupiter.api.Test;
import rife.bld.Project;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static rife.bld.extension.propertyfile.Calc.ADD;

class PropertyFileOperationTest {
    @Test
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    void testExecute() throws Exception {
        var tmpFile = File.createTempFile("property-file-", "properties");
        tmpFile.deleteOnExit();

        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .comment("This is a comment")
                .failOnWarning(true)
                .entry(new EntryInt("version.major").defaultValue(0).calc(ADD))
                .entry(new EntryInt("version.minor").set(0))
                .entry(new EntryInt("version.patch").set(0))
                .entry(new EntryDate("build.date").now().pattern("yyyy-MM-dd"))
                .execute();

        var p = new Properties();
        p.load(Files.newInputStream(tmpFile.toPath()));

        assertThat(p.getProperty("version.major")).as("major").isEqualTo("1");
        assertThat(p.getProperty("version.minor")).as("minor").isEqualTo("0");
        assertThat(p.getProperty("version.patch")).as("patch").isEqualTo("0");
        assertThat(p.getProperty("build.date")).as("date")
                .isEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile.getAbsolutePath())
                .entry(new EntryInt("version.major").calc(c -> c + 2))
                .execute();

        p.load(Files.newInputStream(tmpFile.toPath()));
        assertThat(p.getProperty("version.major")).as("major+2").isEqualTo("3");

        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .entry(new EntryInt("build.date").delete())
                .execute();

        p.clear();
        p.load(Files.newInputStream(tmpFile.toPath()));

        assertThat(p.getProperty("build.date")).as("dalete build.date").isNull();
        assertThat(p).as("version keys").containsKeys("version.major", "version.minor", "version.patch");
    }

    @Test
    void testExecuteNoProject() {
        var op = new PropertyFileOperation();
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
    }
}
