/*
 * Copyright 2023-2025 the original author or authors.
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

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import rife.bld.Project;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static rife.bld.extension.propertyfile.Calc.ADD;

@DisplayName("PropertyFile Operation Tests")
class PropertyFileOperationTest {
    private static final String BUILD_DATE = "build.date";
    private static final String COMMENT = "This is a comment";
    private static final String FOO = "foo";
    private static final String VERSION_MAJOR = "version.major";
    private static final String VERSION_MINOR = "version.minor";
    private static final String VERSION_PATCH = "version.patch";
    private Properties properties;
    private File tmpFile;

    private void loadProperties() throws IOException {
        properties.clear();
        properties.load(Files.newInputStream(tmpFile.toPath()));
    }

    @BeforeEach
    void setUp() throws IOException {
        tmpFile = File.createTempFile("bld-property-file-", ".properties");
        tmpFile.deleteOnExit();
        properties = new Properties();
    }

    @Test
    void shouldClear() throws Exception {
        var bar = "bar";

        properties.setProperty(FOO, bar);
        assertThat(properties).as("properties should not be empty").isNotEmpty();

        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .clear()
                .execute();

        loadProperties();
        assertThat(properties).as("properties should be empty").isEmpty();

        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .clear()
                .entry(new Entry(FOO).set(bar))
                .execute();

        loadProperties();
        assertThat(properties).size().as("properties size should be 1").isEqualTo(1);
        assertThat(properties.getProperty(FOO)).as("foo property should be set").isEqualTo(bar);
    }

    @Test
    void shouldDeleteBuildDateProperty() throws Exception {
        // when
        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .entry(new EntryInt(VERSION_MAJOR).set(1))
                .entry(new EntryInt(VERSION_MINOR).defaultValue(0))
                .entry(new EntryInt(VERSION_PATCH).defaultValue(0))
                .entry(new EntryInt(BUILD_DATE).delete())
                .execute();

        // then
        loadProperties();
        assertThat(properties.getProperty(BUILD_DATE)).as("deleted build.date").isNull();
        assertThat(properties).as("version keys")
                .containsKeys(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH);
    }

    @Test
    void shouldIncrementMajorVersionByTwo() throws Exception {
        // when
        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile.getAbsolutePath())
                .entry(new EntryInt(VERSION_MAJOR).defaultValue(1).calc(c -> c + 2))
                .execute();

        // then
        loadProperties();
        assertThat(properties.getProperty(VERSION_MAJOR)).isEqualTo("3");
    }

    @Test
    void shouldInitializeVersionProperties() throws Exception {
        // when
        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .comment(COMMENT)
                .failOnWarning(true)
                .entry(new EntryInt(VERSION_MAJOR).defaultValue(0).calc(ADD))
                .entry(new EntryInt(VERSION_MINOR).set(0))
                .entry(new EntryInt(VERSION_PATCH).set(0))
                .entry(new EntryDate(BUILD_DATE).now().pattern("yyyy-MM-dd"))
                .execute();
        // then
        loadProperties();
        try (var softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(properties.getProperty(VERSION_MAJOR)).as("major").isEqualTo("1");
            softly.assertThat(properties.getProperty(VERSION_MINOR)).as("minor").isEqualTo("0");
            softly.assertThat(properties.getProperty(VERSION_PATCH)).as("patch").isEqualTo("0");
            softly.assertThat(properties.getProperty(BUILD_DATE)).as("date")
                    .isEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    @Test
    void shouldThrowExceptionWhenNoProject() {
        var op = new PropertyFileOperation();
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
    }

    @Nested
    @DisplayName("File Input Tests")
    class FileInputTests {
        private static final File FOO_FILE = new File(FOO);

        @Test
        void shouldHandleFile() {
            var op = new PropertyFileOperation().file(FOO_FILE);
            assertThat(op.file()).as("as file").isEqualTo(FOO_FILE);
        }

        @Test
        void shouldHandlePath() {
            var op = new PropertyFileOperation().file(FOO_FILE.toPath());
            assertThat(op.file()).as("as path").isEqualTo(FOO_FILE);
        }

        @Test
        void shouldHandleString() {
            var op = new PropertyFileOperation().file(FOO);
            assertThat(op.file()).as("as string").isEqualTo(FOO_FILE);
        }
    }
}
