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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import rife.bld.extension.testing.LoggingExtension;
import rife.bld.extension.testing.TestLogHandler;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Property File Utils Tests")
@ExtendWith(LoggingExtension.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PropertyFileUtilsTest {

    private static final String DEFAULT_INT_PATTERN = "0000";

    @SuppressWarnings("LoggerInitializedWithForeignClass")
    private static final Logger LOGGER = Logger.getLogger(PropertyFileUtils.class.getName());
    private static final Properties PROPERTIES = new Properties();
    private static final TestLogHandler TEST_LOG_HANDLER = new TestLogHandler();

    @RegisterExtension
    @SuppressWarnings("unused")
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(
            LOGGER,
            TEST_LOG_HANDLER,
            Level.ALL
    );

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
    void shouldSaveAndLoadProperties() throws Exception {
        var properties = new Properties();
        properties.put(TEST_VALUE, TEST_VALUE);
        var tempFile = File.createTempFile(TEST_VALUE, ".properties");

        assertThatCode(() ->
                PropertyFileUtils.saveProperties(tempFile, "Generated file - do not modify!", properties))
                .as("save properties").doesNotThrowAnyException();

        assertThat(PropertyFileUtils.loadProperties(TEST_VALUE, tempFile, properties, false, false))
                .as("load properties").isTrue();
        assertThat(properties.getProperty(TEST_VALUE)).as("%s property", TEST_VALUE).isEqualTo(TEST_VALUE);

        tempFile.deleteOnExit();
    }

    @Nested
    @DisplayName("Current Value Tests")
    class CurrentValueTest {

        @Test
        @SuppressWarnings("ConstantValue")
        void shouldHandleAllNullValues() {
            var result = PropertyFileUtils.currentValue(null, null, null);
            assertThat(result).as("all nulls returns null").isNull();
        }

        @Test
        void shouldReturnDefaultValueWhenValueIsNull() {
            var result = PropertyFileUtils.currentValue(null, "default", null);
            assertThat(result).as("default value when value is null").isEqualTo("default");
        }

        @Test
        @SuppressWarnings("ObviousNullCheck")
        void shouldReturnNewValueWhenProvided() {
            var result = PropertyFileUtils.currentValue("old", "default", "new");
            assertThat(result).as("new value takes precedence").isEqualTo("new");
        }

        @Test
        void shouldReturnValueWhenNoNewValueOrDefault() {
            var result = PropertyFileUtils.currentValue("existing", null, null);
            assertThat(result).as("existing value when no new value").isEqualTo("existing");
        }
    }

    @Nested
    @DisplayName("Date Operations Tests")
    class DateOperationsTest {

        @Test
        void shouldDecrementDate() {
            var entryDate = createDateEntry().calc(Calc.SUB);

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now());
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("%s - 1 day", entryDate.key())
                    .isEqualTo(String.valueOf(LocalDateTime.now().minusDays(1).getDayOfYear()));

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now().calc(v -> v - 3));
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("%s - 3 days", entryDate.key())
                    .isEqualTo(String.valueOf(LocalDateTime.now().minusDays(3).getDayOfYear()));
        }

        @Test
        void shouldHaveDefaultDateValue() {
            var now = LocalDateTime.now();
            var entryDate = createDateEntry().defaultValue(now);

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now());
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("%s == day of year", entryDate.key())
                    .isEqualTo(String.valueOf(now.getDayOfYear()));
        }

        @Test
        void shouldIncrementDate() {
            var entryDate = createDateEntry();
            entryDate.calc(Calc.ADD);

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now());
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("%s + 1 day", entryDate.key())
                    .isEqualTo(String.valueOf(LocalDateTime.now().plusDays(1).getDayOfYear()));

            PropertyFileUtils.processDate(PROPERTIES, entryDate.now().calc(v -> v + 3));
            assertThat(PROPERTIES.getProperty(entryDate.key())).as("%s + 3 days", entryDate.key())
                    .isEqualTo(String.valueOf(LocalDateTime.now().plusDays(3).getDayOfYear()));
        }
    }

    @Nested
    @DisplayName("Integer Operations Tests")
    class IntegerOperationsTest {

        @Test
        void shouldDecrementInteger() {
            var entryInt = createIntEntry()
                    .calc(Calc.SUB)
                    .pattern(DEFAULT_INT_PATTERN)
                    .defaultValue("0017");

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("%s == 00016", entryInt.key()).isEqualTo("0016");

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("%s == 00015", entryInt.key()).isEqualTo("0015");
        }

        @Test
        void shouldHaveDefaultIntValue() {
            var entryInt = createIntEntry()
                    .defaultValue("0")
                    .pattern(DEFAULT_INT_PATTERN);

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("%s == 0000", entryInt.key())
                    .isEqualTo("0000");
        }

        @Test
        void shouldIncrementInteger() {
            var entryInt = createIntEntry()
                    .calc(Calc.ADD)
                    .defaultValue("0")
                    .pattern(DEFAULT_INT_PATTERN);

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("%s == 0001", entryInt.key()).isEqualTo("0001");

            PropertyFileUtils.processInt(PROPERTIES, entryInt);
            assertThat(PROPERTIES.getProperty(entryInt.key())).as("%s == 0002", entryInt.key()).isEqualTo("0002");
        }
    }

    @Nested
    @DisplayName("Load Properties Error Handling Tests")
    class LoadPropertiesErrorTest {

        @Test
        void shouldFailWhenFileIsNull() {
            var properties = new Properties();

            assertThatThrownBy(() ->
                    PropertyFileUtils.loadProperties("test", null, properties, true, false))
                    .isInstanceOf(ExitStatusException.class);
            assertThat(TEST_LOG_HANDLER.containsMessage("Please specify the properties file location.")).isTrue();
        }

        @Test
        void shouldFailWithSilentMode() {
            var properties = new Properties();
            assertThatThrownBy(() ->
                    PropertyFileUtils.loadProperties("test", null, properties, true, true))
                    .isInstanceOf(ExitStatusException.class);
            assertThat(TEST_LOG_HANDLER.isEmpty()).isTrue();
        }

        @Test
        void shouldFailWhenFileDoesNotExist() {
            var properties = new Properties();
            var nonExistentFile = new File("nonexistent.properties");
            assertThatThrownBy(() ->
                    PropertyFileUtils.loadProperties("test", nonExistentFile, properties, true, false))
                    .isInstanceOf(ExitStatusException.class);
            assertThat(TEST_LOG_HANDLER.containsMessage("Properties file does not exist:")).isTrue();
        }

        @Test
        void shouldNotSucceedWhenFileIsNull() throws ExitStatusException {
            var properties = new Properties();
            var result =
                    PropertyFileUtils.loadProperties("test", null, properties,
                            false, false);
            assertThat(result).isFalse();
            assertThat(TEST_LOG_HANDLER.containsMessage("Please specify the properties file location.")).isTrue();
        }

        @Test
        void shouldNotSucceedWhenFileDoesNotExist() {
            var properties = new Properties();
            var nonExistentFile = new File("nonexistent.properties");
            assertThatThrownBy(() ->
                    PropertyFileUtils.loadProperties("test", nonExistentFile, properties, true, false))
                    .isInstanceOf(ExitStatusException.class);
            assertThat(TEST_LOG_HANDLER.containsMessage("Properties file does not exist:")).isTrue();
        }

        @Test
        void shouldFailWhenFileIsNotLoading() throws ExitStatusException {
            var properties = new Properties();
            var nonExistentFile = new File("src");
            var result =
                    PropertyFileUtils.loadProperties("test", nonExistentFile, properties,
                            false, false);
            assertThat(result).isFalse();
            assertThat(TEST_LOG_HANDLER.containsMessage("Could not load properties file:")).isTrue();
        }

        @Test
        void shouldFailWhenFileDoesNotExists() throws ExitStatusException {
            var properties = new Properties();
            var nonExistentFile = new File("nonexistent.properties");
            var result =
                    PropertyFileUtils.loadProperties("test", nonExistentFile, properties,
                            false, false);
            assertThat(result).isFalse();
            assertThat(TEST_LOG_HANDLER.containsMessage("Properties file does not exist:")).isTrue();
        }
    }

    @Nested
    @DisplayName("Process Date Additional Tests")
    class ProcessDateAdditionalTest {

        @Test
        void shouldHandleCalendarType() {
            var properties = new Properties();
            var calendar = Calendar.getInstance();
            calendar.set(2025, Calendar.JANUARY, 15, 10, 30, 0);
            var entryDate = new EntryDate("testCalendar");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue(calendar);
            entryDate.unit(EntryDate.Units.DAY);
            entryDate.calc(v -> v + 1);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("calendar date")
                    .contains("2025-01-16");
        }

        @Test
        void shouldHandleDateType() {
            var properties = new Properties();
            var calendar = Calendar.getInstance();
            calendar.set(2025, Calendar.JANUARY, 15);
            var date = calendar.getTime();
            var entryDate = new EntryDate("testDate");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue(date);
            entryDate.unit(EntryDate.Units.DAY);
            entryDate.calc(v -> v + 1);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("java.util.Date")
                    .contains("2025-01-16");
        }

        @Test
        void shouldHandleDateWithoutOffset() {
            var properties = new Properties();
            var today = LocalDate.of(2025, 1, 15);
            var entryDate = new EntryDate("testDate");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue(today);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("date without offset")
                    .isEqualTo("2025-01-15");
        }

        @Test
        void shouldHandleDateWithoutPattern() {
            var properties = new Properties();
            var entryDate = new EntryDate("testDate");
            entryDate.newValue("now");

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("date without pattern")
                    .isNotBlank();
        }

        @Test
        void shouldHandleInstantType() {
            var properties = new Properties();
            var instant = Instant.parse("2025-01-15T10:30:00Z");
            var entryDate = new EntryDate("testInstant");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue(instant);
            entryDate.unit(EntryDate.Units.DAY);
            entryDate.calc(v -> v + 1);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("Instant")
                    .contains("2025-01-16");
        }

        @Test
        void shouldHandleLocalDateTimeType() {
            var properties = new Properties();
            var localDateTime = LocalDateTime.of(2025, 1, 15, 10, 30);
            var entryDate = new EntryDate("testLocalDateTime");
            entryDate.pattern("yyyy-MM-dd HH:mm");
            entryDate.newValue(localDateTime);
            entryDate.unit(EntryDate.Units.DAY);
            entryDate.calc(v -> v + 1);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("LocalDateTime")
                    .isEqualTo("2025-01-16 10:30");
        }

        @Test
        void shouldHandleLocalDateWithDayUnit() {
            var properties = new Properties();
            var localDate = LocalDate.of(2025, 1, 15);
            var entryDate = new EntryDate("testLocalDateTime");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue(localDate);
            entryDate.unit(EntryDate.Units.DAY);
            entryDate.calc(v -> v + 1);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("LocalDate")
                    .isEqualTo("2025-01-16");
        }

        @Test
        void shouldHandleLocalDateWithMonthUnit() {
            var properties = new Properties();
            var entryDate = new EntryDate("testDate");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue(LocalDate.of(2025, 1, 15));
            entryDate.unit(EntryDate.Units.MONTH);
            entryDate.calc(v -> v + 1);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("date + 1 month")
                    .isEqualTo("2025-02-15");
        }

        @Test
        void shouldHandleLocalDateWithWeekUnit() {
            var properties = new Properties();
            var entryDate = new EntryDate("testDate");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue(LocalDate.of(2025, 1, 15));
            entryDate.unit(EntryDate.Units.WEEK);
            entryDate.calc(v -> v + 2);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("date + 2 weeks")
                    .isEqualTo("2025-01-29");
        }

        @Test
        void shouldHandleLocalDateWithYearUnit() {
            var properties = new Properties();
            var localDate = LocalDate.of(2025, 1, 15);
            var entryDate = new EntryDate("testLocalDateTime");
            entryDate.pattern("yyyy");
            entryDate.newValue(localDate);
            entryDate.unit(EntryDate.Units.YEAR);
            entryDate.calc(v -> v + 1);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("LocalDate")
                    .isEqualTo("2026");
        }

        @Test
        void shouldHandleLocalTimeWithHourUnit() {
            var properties = new Properties();
            var entryDate = new EntryDate("testTime");
            entryDate.pattern("HH:mm:ss");
            entryDate.newValue(LocalTime.of(10, 30, 45));
            entryDate.unit(EntryDate.Units.HOUR);
            entryDate.calc(v -> v + 2);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("time + 2 hours")
                    .isEqualTo("12:30:45");
        }

        @Test
        void shouldHandleLocalTimeWithMinuteUnit() {
            var properties = new Properties();
            var entryDate = new EntryDate("testTime");
            entryDate.pattern("HH:mm:ss");
            entryDate.newValue(LocalTime.of(10, 30, 45));
            entryDate.unit(EntryDate.Units.MINUTE);
            entryDate.calc(v -> v + 30);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("time + 30 minutes")
                    .isEqualTo("11:00:45");
        }

        @Test
        void shouldHandleLocalTimeWithSecondUnit() {
            var properties = new Properties();
            var entryDate = new EntryDate("testTime");
            entryDate.pattern("HH:mm:ss");
            entryDate.newValue(LocalTime.of(10, 30, 45));
            entryDate.unit(EntryDate.Units.SECOND);
            entryDate.calc(v -> v + 45);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("time + 45 seconds")
                    .isEqualTo("10:31:30");
        }

        @Test
        void shouldHandleZonedDateTimeWithAllUnits() {
            var properties = new Properties();
            var now = ZonedDateTime.of(2025, 1, 15, 10, 30, 45, 0,
                    ZoneId.systemDefault());
            var entryDate = new EntryDate("testDateTime");
            entryDate.pattern("yyyy-MM-dd HH:mm:ss");
            entryDate.newValue(now);
            entryDate.unit(EntryDate.Units.SECOND);
            entryDate.calc(v -> v + 30);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("datetime + 30 seconds")
                    .isEqualTo("2025-01-15 10:31:15");
        }

        @Test
        void shouldHandleZonedDateTimeWithDayUnit() {
            var properties = new Properties();
            var now = ZonedDateTime.of(2025, 1, 15, 10, 0, 0, 0,
                    ZoneId.systemDefault());
            var entryDate = new EntryDate("testDateTime");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue(now);
            entryDate.unit(EntryDate.Units.DAY);
            entryDate.calc(v -> v + 5);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("datetime + 5 days")
                    .isEqualTo("2025-01-20");
        }

        @Test
        void shouldHandleZonedDateTimeWithHourUnit() {
            var properties = new Properties();
            var now = ZonedDateTime.of(2025, 1, 15, 10, 30, 0, 0,
                    ZoneId.systemDefault());
            var entryDate = new EntryDate("testDateTime");
            entryDate.pattern("yyyy-MM-dd HH:mm:ss");
            entryDate.newValue(now);
            entryDate.unit(EntryDate.Units.HOUR);
            entryDate.calc(v -> v + 5);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("datetime + 5 hours")
                    .isEqualTo("2025-01-15 15:30:00");
        }

        @Test
        void shouldHandleZonedDateTimeWithMinuteUnit() {
            var properties = new Properties();
            var now = ZonedDateTime.of(2025, 1, 15, 10, 30, 0, 0,
                    ZoneId.systemDefault());
            var entryDate = new EntryDate("testDateTime");
            entryDate.pattern("yyyy-MM-dd HH:mm:ss");
            entryDate.newValue(now);
            entryDate.unit(EntryDate.Units.MINUTE);
            entryDate.calc(v -> v + 45);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("datetime + 45 minutes")
                    .isEqualTo("2025-01-15 11:15:00");
        }

        @Test
        void shouldHandleZonedDateTimeWithMonthUnit() {
            var properties = new Properties();
            var now = ZonedDateTime.of(2025, 1, 15, 10, 30, 0, 0,
                    ZoneId.systemDefault());
            var entryDate = new EntryDate("testDateTime");
            entryDate.pattern("yyyy-MM-dd HH:mm");
            entryDate.newValue(now);
            entryDate.unit(EntryDate.Units.MONTH);
            entryDate.calc(v -> v + 3);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("datetime + 3 months")
                    .isEqualTo("2025-04-15 10:30");
        }

        @Test
        void shouldHandleZonedDateTimeWithWeekUnit() {
            var properties = new Properties();
            var now = ZonedDateTime.of(2025, 1, 15, 10, 30, 0, 0,
                    ZoneId.systemDefault());
            var entryDate = new EntryDate("testDateTime");
            entryDate.pattern("yyyy-MM-dd HH:mm");
            entryDate.newValue(now);
            entryDate.unit(EntryDate.Units.WEEK);
            entryDate.calc(v -> v + 2);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("datetime + 2 weeks")
                    .isEqualTo("2025-01-29 10:30");
        }

        @Test
        void shouldHandleZonedDateTimeWithYearUnit() {
            var properties = new Properties();
            var now = ZonedDateTime.of(2025, 1, 15, 10, 30, 0, 0,
                    ZoneId.systemDefault());
            var entryDate = new EntryDate("testDateTime");
            entryDate.pattern("yyyy-MM-dd HH:mm");
            entryDate.newValue(now);
            entryDate.unit(EntryDate.Units.YEAR);
            entryDate.calc(v -> v + 2);

            PropertyFileUtils.processDate(properties, entryDate);
            assertThat(properties.getProperty(entryDate.key())).as("datetime + 2 years")
                    .isEqualTo("2027-01-15 10:30");
        }

        @Test
        void shouldThrowExceptionForInvalidDateType() {
            var properties = new Properties();
            var entryDate = new EntryDate("testDate");
            entryDate.pattern("yyyy-MM-dd");
            entryDate.newValue("invalid-date-string");

            assertThatCode(() -> PropertyFileUtils.processDate(properties, entryDate))
                    .as("invalid date type").isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Non-date value");
        }
    }

    @Nested
    @DisplayName("Process Int Additional Tests")
    class ProcessIntAdditionalTest {

        @Test
        void shouldHandleIntegerWithCustomCalculation() {
            var properties = new Properties();
            var entryInt = new EntryInt("customCalc");
            entryInt.defaultValue("10");
            entryInt.pattern("0000");
            entryInt.calc(v -> v * 2);

            PropertyFileUtils.processInt(properties, entryInt);
            assertThat(properties.getProperty(entryInt.key())).as("10 * 2").isEqualTo("0020");
        }

        @Test
        void shouldHandleIntegerWithExistingPropertyValue() {
            var properties = new Properties();
            properties.setProperty("counter", "0100");
            var entryInt = new EntryInt("counter");
            entryInt.pattern("0000");
            entryInt.calc(Calc.ADD);

            PropertyFileUtils.processInt(properties, entryInt);
            assertThat(properties.getProperty(entryInt.key())).as("100 + 1").isEqualTo("0101");
        }

        @Test
        void shouldHandleIntegerWithoutCalculation() {
            var properties = new Properties();
            var entryInt = new EntryInt("simple");
            entryInt.defaultValue("42");
            entryInt.pattern("0000");

            PropertyFileUtils.processInt(properties, entryInt);
            assertThat(properties.getProperty(entryInt.key())).as("42").isEqualTo("0042");
        }

        @Test
        void shouldHandleNegativeNumbers() {
            var properties = new Properties();
            var entryInt = new EntryInt("negative");
            entryInt.defaultValue("-10");
            entryInt.pattern("0000");
            entryInt.calc(v -> v - 5);

            PropertyFileUtils.processInt(properties, entryInt);
            assertThat(properties.getProperty(entryInt.key())).as("-10 - 5").isEqualTo("-0015");
        }

        @Test
        void shouldHandleNewValue() {
            var properties = new Properties();
            var entryInt = new EntryInt("newVal");
            entryInt.newValue("50");
            entryInt.pattern("0000");
            entryInt.calc(v -> v + 10);

            PropertyFileUtils.processInt(properties, entryInt);
            assertThat(properties.getProperty(entryInt.key())).as("50 + 10").isEqualTo("0060");
        }

        @Test
        void shouldHandleNullCurrentValue() {
            var properties = new Properties();
            var entryInt = new EntryInt("nullValue");
            entryInt.pattern("0000");
            entryInt.calc(v -> v + 5);

            PropertyFileUtils.processInt(properties, entryInt);
            assertThat(properties.getProperty(entryInt.key())).as("null + 5").isEqualTo("0005");
        }

        @Test
        void shouldThrowExceptionForInvalidIntegerValue() {
            var properties = new Properties();
            properties.setProperty("invalid", "not-a-number");
            var entryInt = new EntryInt("invalid");
            entryInt.pattern("0000");

            assertThatCode(() -> PropertyFileUtils.processInt(properties, entryInt))
                    .as("invalid integer value").isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Non-integer value");
        }
    }

    @Nested
    @DisplayName("Process String Additional Tests")
    class ProcessStringAdditionalTest {

        @Test
        void shouldHandleExistingPropertyWithoutNewValue() {
            var properties = new Properties();
            properties.setProperty("prop", "existing");
            var entry = new Entry("prop");

            PropertyFileUtils.processString(properties, entry);
            assertThat(properties.getProperty(entry.key())).as("keep existing")
                    .isEqualTo("existing");
        }

        @Test
        void shouldHandleStringWithDefaultValue() {
            var properties = new Properties();
            var entry = new Entry("withDefault");
            entry.defaultValue("default-value");

            PropertyFileUtils.processString(properties, entry);
            assertThat(properties.getProperty(entry.key())).as("default value")
                    .isEqualTo("default-value");
        }

        @Test
        void shouldHandleStringWithExistingProperty() {
            var properties = new Properties();
            properties.setProperty("existing", "old-value");
            var entry = new Entry("existing");
            entry.newValue("new-value");

            PropertyFileUtils.processString(properties, entry);
            assertThat(properties.getProperty(entry.key())).as("overwritten value")
                    .isEqualTo("new-value");
        }

        @Test
        void shouldHandleStringWithPattern() {
            var properties = new Properties();
            var entry = new Entry("formatted");
            entry.newValue("test");
            entry = entry.pattern("Value: %s");

            PropertyFileUtils.processString(properties, entry);
            assertThat(properties.getProperty(entry.key())).as("formatted string")
                    .isEqualTo("Value: test");
        }

        @Test
        void shouldHandleStringWithoutModify() {
            var properties = new Properties();
            var entry = new Entry("simple");
            entry.newValue("simple-value");

            PropertyFileUtils.processString(properties, entry);
            assertThat(properties.getProperty(entry.key())).as("simple value")
                    .isEqualTo("simple-value");
        }
    }

    @Nested
    @DisplayName("Save Properties Tests")
    class SavePropertiesTest {

        @Test
        void shouldSavePropertiesWithEmptyComment() throws Exception {
            var properties = new Properties();
            properties.put("test", "value");
            var tempFile = File.createTempFile("test", ".properties");

            assertThatCode(() ->
                    PropertyFileUtils.saveProperties(tempFile, "", properties))
                    .as("save with empty comment").doesNotThrowAnyException();

            tempFile.deleteOnExit();
        }

        @Test
        void shouldSavePropertiesWithNullComment() throws Exception {
            var properties = new Properties();
            properties.put("test", "value");
            var tempFile = File.createTempFile("test", ".properties");

            assertThatCode(() ->
                    PropertyFileUtils.saveProperties(tempFile, null, properties))
                    .as("save with null comment").doesNotThrowAnyException();

            tempFile.deleteOnExit();
        }

        @Test
        void shouldThrowExceptionForInvalidPath() {
            var properties = new Properties();
            properties.put("key", "value");
            var invalidFile = new File("/invalid/path/that/does/not/exist/file.properties");

            assertThatCode(() ->
                    PropertyFileUtils.saveProperties(invalidFile, "comment", properties))
                    .as("invalid file path").isInstanceOf(IOException.class)
                    .hasMessageContaining("An IO error occurred");
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

    @Nested
    @DisplayName("Warn Method Tests")
    class WarnMethodTests {

        @Test
        void shouldFailOnWarningInSilentMode() {
            assertThatCode(() ->
                    PropertyFileUtils.warn(LOGGER, "command", "silent message", true, true))
                    .as("should fail on warning even in silent mode")
                    .isInstanceOf(ExitStatusException.class);
        }

        @Test
        void shouldFailOnWarningWhenFlagIsTrue() {
            assertThatCode(() ->
                    PropertyFileUtils.warn(LOGGER, "command", "message", true, false))
                    .as("should fail on warning when failOnWarning=true")
                    .isInstanceOf(ExitStatusException.class);
            assertThat(TEST_LOG_HANDLER.containsMessage("[command] message"))
                    .as("should log SEVERE message")
                    .isTrue();
        }

        @Test
        void shouldFailWithInfoLogging() throws ExitStatusException {
            TEST_LOG_HANDLER.clear();
            LOGGER.setLevel(Level.OFF);
            PropertyFileUtils.warn(LOGGER, "command", "message", false, true);
            assertThat(TEST_LOG_HANDLER.isEmpty()).isTrue();
        }

        @Test
        void shouldFailWithNoLogging() {
            TEST_LOG_HANDLER.clear();
            LOGGER.setLevel(Level.OFF);
            assertThatCode(() ->
                    PropertyFileUtils.warn(LOGGER, "command", "message", true, true))
                    .isInstanceOf(ExitStatusException.class);
            assertThat(TEST_LOG_HANDLER.isEmpty()).isTrue();
        }

        @Test
        void shouldLogWithCommandPrefix() {
            TEST_LOG_HANDLER.clear();
            assertThatCode(() ->
                    PropertyFileUtils.warn(LOGGER, "myCommand", "test message", false, false))
                    .as("should not throw exception")
                    .doesNotThrowAnyException();
            assertThat(TEST_LOG_HANDLER.containsMessage("[myCommand] test message"))
                    .as("should include command prefix in log")
                    .isTrue();
        }

        @Test
        void shouldNotFailOnWarningWhenFlagIsFalse() {
            assertThatCode(() ->
                    PropertyFileUtils.warn(LOGGER, "command", TEST_VALUE, false, false))
                    .as("should not fail on warning when failOnWarning=false")
                    .doesNotThrowAnyException();
            assertThat(TEST_LOG_HANDLER.containsMessage("[command] " + TEST_VALUE))
                    .as("should log WARNING message")
                    .isTrue();
        }

        @Test
        void shouldNotLogInSilentModeWhenFailOnWarningIsFalse() {
            TEST_LOG_HANDLER.clear();
            assertThatCode(() ->
                    PropertyFileUtils.warn(LOGGER, "testCmd", "silent warning", false, true))
                    .as("should not throw exception")
                    .doesNotThrowAnyException();
            assertThat(TEST_LOG_HANDLER.containsMessage("silent warning"))
                    .as("should not log in silent mode")
                    .isFalse();
        }

        @Test
        void shouldNotLogInSilentModeWhenFailOnWarningIsTrue() {
            TEST_LOG_HANDLER.clear();
            assertThatCode(() ->
                    PropertyFileUtils.warn(LOGGER, "testCmd", "silent severe", true, true))
                    .as("should throw exception")
                    .isInstanceOf(ExitStatusException.class);
            assertThat(TEST_LOG_HANDLER.containsMessage("silent severe"))
                    .as("should not log in silent mode")
                    .isFalse();
        }
    }
}