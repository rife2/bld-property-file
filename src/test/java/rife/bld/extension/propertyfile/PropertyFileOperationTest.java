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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static rife.bld.extension.propertyfile.Calc.ADD;

@DisplayName("PropertyFile Operation Tests")
@ExtendWith(LoggingExtension.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
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

    @Test
    void isClearChecks() {
        var op = new PropertyFileOperation();
        assertThat(op.isClear()).isFalse();

        op.clear();
        assertThat(op.isClear()).isTrue();

        op.clear(false);
        assertThat(op.isClear()).isFalse();

        op.clear(true);
        assertThat(op.isClear()).isTrue();
    }

    @Test
    void isFailOnWarningsChecks() {
        var op = new PropertyFileOperation();
        assertThat(op.isFailOnWarning()).isFalse();

        op.failOnWarning(true);
        assertThat(op.isFailOnWarning()).isTrue();
    }

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
        var entry = new Entry(FOO).set(bar);

        var op = new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .clear()
                .entry(entry);

        assertThat(op.entries()).containsOnly(entry);

        op.execute();
        assertThat(TEST_LOG_HANDLER.containsMessage("All entries will be cleared first.")).isTrue();

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
                .entry(entry)
                .execute();

        loadProperties();
        assertThat(properties).size().as("properties size should be 1").isEqualTo(1);
        assertThat(properties.getProperty(FOO)).as("%s property should be set", FOO).isEqualTo(bar);
    }

    @Test
    void shouldClearWithoutWarning() throws Exception {
        LOGGER.setLevel(Level.OFF);
        var bar = "bar";
        var entry = new Entry(FOO).set(bar);

        var op = new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .clear()
                .entry(entry);
        op.execute();

        assertThat(op.entries()).containsOnly(entry);
        assertThat(TEST_LOG_HANDLER.containsMessage("All entries will be cleared first.")).isFalse();
    }

    @Test
    void shouldDeleteBuildDateProperty() throws Exception {
        var major = new EntryInt(VERSION_MAJOR).set(1);
        var minor = new EntryInt(VERSION_MINOR).defaultValue(0);
        var patch = new EntryInt(VERSION_PATCH).defaultValue(0);
        var date = new EntryInt(BUILD_DATE).delete();
        var op = new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .entry(major)
                .entry(minor)
                .entry(patch)
                .entry(date);
        op.execute();

        assertThat(op.entries()).containsOnly(major, minor, patch, date);

        loadProperties();
        assertThat(properties.getProperty(BUILD_DATE)).as("%s should be deleted", BUILD_DATE).isNull();
        assertThat(properties).containsOnlyKeys(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH);
    }

    @Test
    void shouldHaveDefaultValue() {
        var major = new EntryInt(VERSION_MAJOR);
        var op = new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .failOnWarning(true)
                .entry(major);

        assertThat(op.entries()).containsOnly(major);
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        assertThat(TEST_LOG_HANDLER.containsMessage("An entry must be set or have a default value: version.major"))
                .isTrue();
    }

    @Test
    void shouldHavePropertiesFile() {
        var op = new PropertyFileOperation().fromProject(new Project()).failOnWarning(true);

        assertThat(op.entries()).isEmpty();
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        assertThat(TEST_LOG_HANDLER.containsMessage("Please specify the properties file location."))
                .isFalse();
    }

    @Test
    void shouldHaveValidPropertiesFile() {
        var op = new PropertyFileOperation()
                .fromProject(new Project())
                .file("foo")
                .failOnWarning(false);
        assertThatThrownBy(op::execute).isInstanceOf(ExitStatusException.class);

        assertThat(op.entries()).isEmpty();
        assertThat(TEST_LOG_HANDLER.containsMessage("Properties file does not exist: foo")).isFalse();
    }

    @Test
    void shouldIncrementMajorVersionByTwo() throws Exception {
        var major = new EntryInt(VERSION_MAJOR);
        var op = new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile.getAbsolutePath())
                .entry(major.defaultValue(1).calc(c -> c + 2));
        op.execute();

        assertThat(op.entries()).containsOnly(major);

        loadProperties();
        assertThat(properties.getProperty(VERSION_MAJOR)).isEqualTo("3");
    }

    @Test
    void shouldInitializeVersionProperties() throws Exception {
        var major = new EntryInt(VERSION_MAJOR).defaultValue(0).calc(ADD);
        var minor = new EntryInt(VERSION_MINOR).set(0);
        var patch = new EntryInt(VERSION_PATCH).set(0);
        var date = new EntryDate(BUILD_DATE).now().pattern("yyyy-MM-dd");
        var op = new PropertyFileOperation()
                .fromProject(new Project())
                .file(tmpFile)
                .comment(COMMENT)
                .failOnWarning(true)
                .entry(major)
                .entry(minor)
                .entry(patch)
                .entry(date);
        op.execute();

        assertThat(op.entries()).containsOnly(major, minor, patch, date);

        loadProperties();
        try (var softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(properties.getProperty(VERSION_MAJOR)).as("%s == 1", VERSION_MAJOR)
                    .isEqualTo("1");
            softly.assertThat(properties.getProperty(VERSION_MINOR)).as("%s == 0", VERSION_MINOR)
                    .isEqualTo("0");
            softly.assertThat(properties.getProperty(VERSION_PATCH)).as("%s == 0", VERSION_PATCH)
                    .isEqualTo("0");
            softly.assertThat(properties.getProperty(BUILD_DATE)).as("%s == now", BUILD_DATE)
                    .isEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    @Test
    void shouldThrowExceptionWhenNoProject() {
        var op = new PropertyFileOperation();
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        assertThat(TEST_LOG_HANDLER.containsMessage("A project is required")).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenNoProjectAndNoLogging() {
        LOGGER.setLevel(Level.OFF);
        var op = new PropertyFileOperation().silent(true);
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        assertThat(TEST_LOG_HANDLER.isEmpty()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenNoProjectAndSilent() {
        var op = new PropertyFileOperation().silent(true);
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        assertThat(TEST_LOG_HANDLER.isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("EntryBase Key Tests")
    class EntryBaseKeyTests {

        @Test
        void shouldAllowMultipleKeyChanges() throws Exception {
            var entry = new Entry("key1").key("key2").key("key3").set("final value");
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty("key3"))
                    .as("key3 should be set (final key)")
                    .isEqualTo("final value");
            assertThat(properties.getProperty("key1"))
                    .as("key1 should not exist")
                    .isNull();
            assertThat(properties.getProperty("key2"))
                    .as("key2 should not exist")
                    .isNull();
        }

        @Test
        void shouldChainKeyWithOtherMethods() throws Exception {
            var entry = new EntryInt("counter")
                    .defaultValue(0)
                    .key("build.number")
                    .calc(ADD)
                    .pattern("0000");
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty("build.number"))
                    .as("build.number should be incremented")
                    .isEqualTo("0001");
        }

        @Test
        void shouldChangeKeyUsingKeyMethod() throws Exception {
            var entry = new Entry("old.key").key("new.key").set("value");
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty("new.key"))
                    .as("new.key should be set")
                    .isEqualTo("value");
            assertThat(properties.getProperty("old.key"))
                    .as("old.key should not exist")
                    .isNull();
        }

        @Test
        void shouldHandleKeyWithSpecialCharacters() throws Exception {
            var entry = new Entry("simple")
                    .key("my.special-key_123")
                    .set("test");
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty("my.special-key_123"))
                    .as("key with special characters should be set")
                    .isEqualTo("test");
        }

        @Test
        void shouldReturnSameInstanceForMethodChaining() {
            var entry = new Entry("test");
            var result = entry.key("new.test");

            assertThat(result)
                    .as("key() should return same instance")
                    .isSameAs(entry);
        }

        @Test
        void shouldSetKeyForEntryDate() throws Exception {
            var testDate = LocalDate.of(2025, 1, 15);
            var entry = new EntryDate("old.date")
                    .key("new.date")
                    .set(testDate)
                    .pattern("yyyy-MM-dd");
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty("new.date"))
                    .as("new.date should be set")
                    .isEqualTo("2025-01-15");
            assertThat(properties.getProperty("old.date"))
                    .as("old.date should not exist")
                    .isNull();
        }

        @Test
        void shouldSetKeyForEntryInt() throws Exception {
            var entry = new EntryInt("initial.counter")
                    .key("final.counter")
                    .set(42);
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty("final.counter"))
                    .as("final.counter should be set")
                    .isEqualTo("42");
            assertThat(properties.getProperty("initial.counter"))
                    .as("initial.counter should not exist")
                    .isNull();
        }

        @Test
        void shouldSetKeyViaConstructor() throws Exception {
            var entry = new Entry("initial.key").set("value1");
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty("initial.key"))
                    .as("initial.key should be set")
                    .isEqualTo("value1");
        }
    }

    @Nested
    @DisplayName("Entry Modify Tests")
    class EntryModifyTests {

        @Test
        void shouldChainSetAndModify() throws Exception {
            var entry = new Entry(FOO)
                    .set("initial value")
                    .modify("VALUE", (current, modifyValue) ->
                            current.replace("value", modifyValue));
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty(FOO))
                    .as("%s should be set and modified", FOO)
                    .isEqualTo("initial VALUE");
        }

        @Test
        void shouldModifyWithBiFunctionOnly() throws Exception {
            var entry = new Entry(FOO).modify((current, modifyValue) -> current.toUpperCase());
            properties.setProperty(FOO, "hello world");
            PropertyFileUtils.saveProperties(tmpFile, "", properties);
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty(FOO))
                    .as("%s should be uppercase", FOO)
                    .isEqualTo("HELLO WORLD");
        }

        @Test
        void shouldModifyWithReplacement() throws Exception {
            var entry = new Entry(FOO).modify("bar",
                    (current, modifyValue) -> current.replace(modifyValue, "BAR"));
            properties.setProperty(FOO, "foo bar baz");
            PropertyFileUtils.saveProperties(tmpFile, "", properties);
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty(FOO))
                    .as("%s should have replaced text", FOO)
                    .isEqualTo("foo BAR baz");
        }

        @Test
        void shouldModifyWithSubstring() throws Exception {
            var entry = new Entry(FOO).modify(
                    (current, modifyValue) -> {
                        int start = current.indexOf("-") + 1;
                        int end = current.lastIndexOf("-");
                        return current.substring(start, end);
                    }
            );
            properties.setProperty(FOO, "prefix-value-suffix");
            PropertyFileUtils.saveProperties(tmpFile, "", properties);
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty(FOO))
                    .as("%s should contain extracted substring", FOO)
                    .isEqualTo("value");
        }

        @Test
        void shouldModifyWithTrim() throws Exception {
            var entry = new Entry(FOO).modify((current, modifyValue) -> current.trim());
            properties.setProperty(FOO, "  hello world  ");
            PropertyFileUtils.saveProperties(tmpFile, "", properties);
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();
            loadProperties();

            assertThat(op.entries()).containsOnly(entry);

            assertThat(properties.getProperty(FOO))
                    .as("%s should be trimmed", FOO)
                    .isEqualTo("hello world");
        }

        @Test
        void shouldModifyWithValueAndBiFunction() throws Exception {
            var entry = new Entry(FOO).modify(" world",
                    (current, modifyValue) -> current + modifyValue);
            properties.setProperty(FOO, "hello");
            PropertyFileUtils.saveProperties(tmpFile, "", properties);
            var op = new PropertyFileOperation()
                    .fromProject(new Project())
                    .file(tmpFile)
                    .entry(entry);
            op.execute();

            assertThat(op.entries()).containsOnly(entry);

            loadProperties();
            assertThat(properties.getProperty(FOO))
                    .as("%s should have appended value", FOO)
                    .isEqualTo("hello world");
        }
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
