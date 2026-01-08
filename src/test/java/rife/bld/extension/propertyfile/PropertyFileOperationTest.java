/*
 * Copyright 2023-2026 the original author or authors.
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import rife.bld.Project;
import rife.bld.extension.testing.LoggingExtension;
import rife.bld.extension.testing.TestLogHandler;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static rife.bld.extension.propertyfile.Calc.ADD;

@DisplayName("PropertyFile Operation Tests")
@ExtendWith(LoggingExtension.class)
class PropertyFileOperationTest {

    private static final String BUILD_DATE = "build.date";
    private static final String COMMENT = "This is a comment";
    private static final String FOO = "foo";

    @SuppressWarnings("LoggerInitializedWithForeignClass")
    private static final Logger LOGGER = Logger.getLogger(PropertyFileOperation.class.getName());
    private static final TestLogHandler TEST_LOG_HANDLER = new TestLogHandler();

    @RegisterExtension
    @SuppressWarnings("unused")
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(
            LOGGER,
            TEST_LOG_HANDLER,
            Level.ALL
    );

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

        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .clear()
                .entry(new Entry(FOO).set(bar))
                .execute();

        assertThat(TEST_LOG_HANDLER.containsMessage("All entries will be cleared first."));

        loadProperties();
        assertThat(properties).as("properties should only contain %s", FOO).containsOnlyKeys(FOO);

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
        assertThat(properties.getProperty(FOO)).as("%s property should be set", FOO).isEqualTo(bar);
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
        assertThat(properties.getProperty(BUILD_DATE)).as("%s should be deleted", BUILD_DATE).isNull();
        assertThat(properties).containsOnlyKeys(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH);
    }

    @Test
    void shouldHaveDefaultValue() throws Exception {
        new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .entry(new EntryInt(VERSION_MAJOR))
                .execute();
        assertThat(TEST_LOG_HANDLER.containsMessage("An entry must be set or have a default value: version.major"));
    }

    @Test
    void shouldHavePropertiesFile() {
        var op = new PropertyFileOperation().fromProject(new Project());
        assertThatCode(op::execute).isInstanceOf(NullPointerException.class);
        assertThat(TEST_LOG_HANDLER.containsMessage("A properties file must be specified."));
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
            softly.assertThat(properties.getProperty(VERSION_MAJOR)).as("%s == 1", VERSION_MAJOR).isEqualTo("1");
            softly.assertThat(properties.getProperty(VERSION_MINOR)).as("%s == 0", VERSION_MINOR).isEqualTo("0");
            softly.assertThat(properties.getProperty(VERSION_PATCH)).as("%s == 0", VERSION_PATCH).isEqualTo("0");
            softly.assertThat(properties.getProperty(BUILD_DATE)).as("%s == now", BUILD_DATE)
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
            assertThat(op.file()).isEqualTo(FOO_FILE);
        }

        @Test
        void shouldHandlePath() {
            var op = new PropertyFileOperation().file(FOO_FILE.toPath());
            assertThat(op.file()).isEqualTo(FOO_FILE);
        }

        @Test
        void shouldHandleString() {
            var op = new PropertyFileOperation().file(FOO);
            assertThat(op.file()).isEqualTo(FOO_FILE);
        }
    }
}
