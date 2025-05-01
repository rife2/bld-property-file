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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static rife.bld.extension.propertyfile.Calc.ADD;
import static rife.bld.extension.propertyfile.Calc.SUB;

class PropertyFileUtilsTest {
    private static final String DEFAULT_INT_PATTERN = "0000";
    private static final Properties PROPERTIES = new Properties();
    private static final String TEST_VALUE = "test";

    private EntryDate createDateEntry() {
        PROPERTIES.clear();
        return new EntryDate("aDate").pattern("D");
    }

    private EntryInt createIntEntry() {
        PROPERTIES.clear();
        return new EntryInt("version.major");
    }

    private Entry createVersionEntry() {
        PROPERTIES.clear();
        return new Entry("version.major").set("1");
    }

    @Test
    void shouldHandleWarnings() {
        assertThatCode(() ->
                PropertyFileUtils.warn("command", "message", true, false))
                .isInstanceOf(ExitStatusException.class);

        assertThatCode(() ->
                PropertyFileUtils.warn("command", TEST_VALUE, false, false))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSaveAndLoadProperties() throws Exception {
        var properties = new Properties();
        properties.put(TEST_VALUE, TEST_VALUE);
        var tempFile = File.createTempFile(TEST_VALUE, ".properties");

        assertThatCode(() ->
                PropertyFileUtils.saveProperties(tempFile, "Generated file - do not modify!", properties))
                .doesNotThrowAnyException();

        assertThat(PropertyFileUtils.loadProperties(TEST_VALUE, tempFile, properties, false)).isTrue();
        assertThat(properties.getProperty(TEST_VALUE)).isEqualTo(TEST_VALUE);

        tempFile.deleteOnExit();
    }

    @Nested
    @DisplayName("Date Operations Tests")
    class DateOperationsTest {
        @Test
        void shouldDecrementDate() {
            var entryDate = createDateEntry().calc(SUB);

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now());
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("-1 day")
                    .isEqualTo(String.valueOf(LocalDateTime.now().minusDays(1).getDayOfYear()));

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now().calc(v -> v - 3));
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("-3 days")
                    .isEqualTo(String.valueOf(LocalDateTime.now().minusDays(3).getDayOfYear()));
        }

        @Test
        void shouldIncrementDate() {
            var entryDate = createDateEntry();
            entryDate.calc(ADD);

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now());
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("+1 day")
                    .isEqualTo(String.valueOf(LocalDateTime.now().plusDays(1).getDayOfYear()));

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now().calc(v -> v + 3));
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("+3 days")
                    .isEqualTo(String.valueOf(LocalDateTime.now().plusDays(3).getDayOfYear()));
        }

        @Test
        void shouldHaveDefaultDateValue() {
            var now = LocalDateTime.now();
            var entryDate = createDateEntry().defaultValue(now);

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now());
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("day of year")
                    .isEqualTo(String.valueOf(now.getDayOfYear()));
        }
    }

    @Nested
    @DisplayName("Integer Operations Tests")
    class IntegerOperationsTest {
        @Test
        void shouldDecrementInteger() {
            var entryInt = createIntEntry()
                    .calc(SUB)
                    .pattern(DEFAULT_INT_PATTERN)
                    .defaultValue("0017");

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("00016").isEqualTo("0016");

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("00015").isEqualTo("0015");
        }

        @Test
        void shouldHaveDefaultIntValue() {
            var entryInt = createIntEntry()
                    .defaultValue("0")
                    .pattern(DEFAULT_INT_PATTERN);

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).isEqualTo("0000");
        }

        @Test
        void shouldIncrementInteger() {
            var entryInt = createIntEntry()
                    .calc(ADD)
                    .defaultValue("0")
                    .pattern(DEFAULT_INT_PATTERN);

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("0001").isEqualTo("0001");

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("0002").isEqualTo("0002");
        }
    }

    @Nested
    @DisplayName("String Operations Tests")
    class StringOperationsTest {
        @Test
        void shouldManipulateStrings() {
            var entry = createVersionEntry();

            PropertyFileUtils.processString(PROPERTIES, entry);
            assertThat(entry.newValue()).isEqualTo(PROPERTIES.getProperty(entry.key()));

            entry.set(TEST_VALUE).modify("T", (v, s) -> v.replace("t", s));
            PropertyFileUtils.processString(PROPERTIES, entry);
            assertThat(PROPERTIES.getProperty(entry.key())).as("replace(t, T)").isEqualTo("TesT");
        }
    }
}