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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import rife.bld.extension.tools.IOTools;
import rife.bld.extension.tools.ObjectTools;
import rife.bld.extension.tools.TextTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

/**
 * Collection of common methods used in this project.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class PropertyFileUtils {

    private PropertyFileUtils() {
        // no-op
    }

    private static String applyOffsetAndFormat(Object value, EntryDate entry, DateTimeFormatter dtf)
            throws DateTimeException {
        var offset = entry.calc() != null ? entry.calc().apply(0) : 0;
        var unit = entry.unit();
        try {
            final TemporalAccessor result;
            if (value instanceof LocalDate ld) {
                result = offset == 0 ? ld : switch (unit) {
                    case DAY -> ld.plusDays(offset);
                    case WEEK -> ld.plusWeeks(offset);
                    case MONTH -> ld.plusMonths(offset);
                    case YEAR -> ld.plusYears(offset);
                    // HOUR, MINUTE, SECOND are not applicable to LocalDate
                    default -> throw new IllegalArgumentException(
                            "Unit " + unit + " is not applicable to a date-only value for \"" + entry.key() + "\"");
                };
            } else if (value instanceof LocalTime lt) {
                result = offset == 0 ? lt : switch (unit) {
                    case SECOND -> lt.plusSeconds(offset);
                    case MINUTE -> lt.plusMinutes(offset);
                    case HOUR -> lt.plusHours(offset);
                    // DAY, WEEK, MONTH, YEAR are not applicable to LocalTime
                    default -> throw new IllegalArgumentException(
                            "Unit " + unit + " is not applicable to a time-only value for \"" + entry.key() + "\"");
                };
            } else {
                var zdt = (ZonedDateTime) value;
                result = offset == 0 ? zdt : switch (unit) {
                    case DAY -> zdt.plusDays(offset);
                    case WEEK -> zdt.plusWeeks(offset);
                    case MONTH -> zdt.plusMonths(offset);
                    case YEAR -> zdt.plusYears(offset);
                    case SECOND -> zdt.plusSeconds(offset);
                    case MINUTE -> zdt.plusMinutes(offset);
                    case HOUR -> zdt.plusHours(offset);
                };
            }
            return dtf.format(result);
        } catch (DateTimeException dte) {
            throw new DateTimeException(
                    "Date arithmetic or formatting error for \"" + entry.key() + "\" --> " + dte.getMessage(), dte);
        }
    }

    /**
     * Returns the new value, value, or default value depending on which is specified.
     *
     * <p>Priority order: {@code newValue} ? {@code value} ? {@code defaultValue}.
     *
     * @param value        the current persisted value; may be {@code null} if the key is absent
     * @param defaultValue the fallback used when both {@code value} and {@code newValue} are {@code null}
     * @param newValue     when non-{@code null}, always takes precedence over the other two
     * @return the resolved object; {@code null} if all three arguments are {@code null}
     */
    @Nullable
    public static Object currentValue(@Nullable String value, @Nullable Object defaultValue,
                                      @Nullable Object newValue) {
        if (newValue != null) {
            return newValue;
        } else if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    /**
     * Loads a {@link Properties properties} file.
     *
     * @param file the file location
     * @param p    the {@link Properties properties} to load into
     * @throws IOException              if an error occurred while reading the file
     * @throws IllegalArgumentException if the file does not exist
     */
    @SuppressFBWarnings(value = {"EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS", "LEST_LOST_EXCEPTION_STACK_TRACE"},
    justification = "IOException is re-thrown as the same type, not softened; cause is always chained.")
    public static void loadProperties(@NonNull File file, @NonNull Properties p) throws IOException {
        if (IOTools.exists(file)) {
            try (var propStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
                p.load(propStream);
            } catch (IOException ioe) {
                throw new IOException("Could not load properties file: " + ioe.getMessage(), ioe);
            }
        } else {
            throw new IllegalArgumentException("Please specify a valid properties file location.");
        }
    }

    private static Object normaliseDateValue(@NonNull Object value, String key) {
        if (value instanceof String s) {
            if (!"now".equalsIgnoreCase(s)) {
                throw new IllegalArgumentException(
                        "Non-date value for \"" + key + "\": expected \"now\", Calendar, Date, or java.time type");
            }
            return ZonedDateTime.now();
        } else if (value instanceof LocalDateTime ldt) {
            return ldt.atZone(ZoneId.systemDefault());
        } else if (value instanceof Date d) {
            return d.toInstant().atZone(ZoneId.systemDefault());
        } else if (value instanceof Calendar c) {
            return c.toInstant().atZone(ZoneId.systemDefault());
        } else if (value instanceof Instant i) {
            return i.atZone(ZoneId.systemDefault());
        } else if (value instanceof ZonedDateTime || value instanceof LocalDate || value instanceof LocalTime) {
            // already in a supported type — return as-is
            return value;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported date type for \"" + key + "\": " + value.getClass().getName());
        }
    }

    /**
     * Processes a date {@link Properties properties}.
     *
     * @param p     the {@link Properties properties}
     * @param entry the {@link Entry} containing the {@link Properties properties} edits
     * @throws DateTimeException        if a parsing or arithmetic error occurs
     * @throws IllegalArgumentException if no value, defaultValue, or newValue is configured and no
     *                                  pattern is set
     */
    public static void processDate(@NonNull Properties p, @NonNull EntryDate entry) throws DateTimeException {
        var currentValue = currentValue(null, entry.defaultValue(), entry.newValue());
        var pattern = Objects.toString(entry.pattern(), "");

        if (TextTools.isNotBlank(pattern)) {
            if (currentValue == null) {
                throw new IllegalArgumentException(
                        "No value, defaultValue, or newValue configured for date entry \"" + entry.key() + "\"");
            }
            var normalised = normaliseDateValue(currentValue, entry.key());
            p.setProperty(entry.key(), applyOffsetAndFormat(normalised, entry, DateTimeFormatter.ofPattern(pattern)));
        } else {
            // No pattern: store the literal string representation.
            // Callers should ensure at least a defaultValue is configured if "null" is not desired.
            p.setProperty(entry.key(), Objects.toString(currentValue, "null"));
        }
    }

    /**
     * Processes an integer {@link Properties properties}.
     *
     * @param p     the {@link Properties properties}
     * @param entry the {@link Entry} containing the {@link Properties properties} edits
     */
    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
    public static void processInt(@NonNull Properties p, @NonNull EntryInt entry) {
        int intValue = 0;
        try {
            var fmt = new DecimalFormat(Objects.toString(entry.pattern(), ""));
            var currentValue = currentValue(p.getProperty(entry.key()), entry.defaultValue(), entry.newValue());

            if (currentValue != null) {
                intValue = fmt.parse(Objects.toString(currentValue, "")).intValue();
            }

            if (entry.calc() != null) {
                intValue = entry.calc().apply(intValue);
            }

            p.setProperty(entry.key(), fmt.format(intValue));
        } catch (NumberFormatException | ParseException e) {
            throw new IllegalArgumentException(
                    "Non-integer value for \"" + entry.key() + "\" --> " + e.getMessage(), e);
        }
    }

    /**
     * Processes a string {@link Properties property}.
     *
     * @param p     the {@link Properties property}
     * @param entry the {@link Entry} containing the {@link Properties property} edits
     */
    @SuppressFBWarnings("FORMAT_STRING_MANIPULATION")
    public static void processString(@NonNull Properties p, @NonNull Entry entry) {
        var currentValue = currentValue(p.getProperty(entry.key()), entry.defaultValue(), entry.newValue());

        // When all value sources are absent, currentValue is null.
        // Objects.toString guards against String.valueOf(null) silently producing "null".
        var resolved = Objects.toString(currentValue, "");

        p.setProperty(entry.key(), entry.pattern() != null
                ? String.format(String.valueOf(entry.pattern()), resolved)
                : resolved);

        if (ObjectTools.isNotNull(entry.modify(), entry.modifyValue())) {
            // modify() transforms the current property value; the result is then used as the format
            // string with entry.pattern() as its argument (intentional inversion by design).
            var modify = entry.modify().apply(p.getProperty(entry.key()), entry.modifyValue());
            p.setProperty(entry.key(), String.format(modify, entry.pattern()));
        }
    }

    /**
     * Saves a {@link Properties properties} file.
     *
     * @param file    the file location
     * @param comment the header comment
     * @param p       the {@link Properties} to save into the file
     * @throws IOException if an IO error occurs while writing the file
     */
    public static void saveProperties(@NonNull File file, String comment, @NonNull Properties p) throws IOException {
        try (var output = Files.newOutputStream(file.toPath())) {
            p.store(output, comment);
        } catch (IOException ioe) {
            // Re-wrap to include the file path in the message for easier diagnosis at the call site.
            throw new IOException("An IO error occurred while saving the Properties file: " + file, ioe);
        }
    }
}