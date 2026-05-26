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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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
 * Utility methods for {@link PropertyFileOperation}.
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
                    default -> throw new IllegalArgumentException(
                            "Unit " + unit + " is not applicable to a date-only value for \"" + entry.key() + "\"");
                };
            } else if (value instanceof LocalTime lt) {
                result = offset == 0 ? lt : switch (unit) {
                    case SECOND -> lt.plusSeconds(offset);
                    case MINUTE -> lt.plusMinutes(offset);
                    case HOUR -> lt.plusHours(offset);
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
     * Loads a {@link Properties properties} file.
     * <p>
     * If the file does not exist, no properties are loaded. The file will be created when
     * {@link #saveProperties(File, String, Properties)} is called.
     *
     * @param file the file location
     * @param p    the {@link Properties properties} to load into
     * @throws IOException          if an error occurred while reading the file
     * @throws NullPointerException if {@code file} or {@code p} is {@code null}
     */
    public static void loadProperties(@NonNull File file, @NonNull Properties p) throws IOException {
        ObjectTools.requireNonNull(file, "properties file");
        ObjectTools.requireNonNull(p, "properties");

        if (IOTools.exists(file)) {
            try (var in = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
                p.load(in);
            } catch (IOException ioe) {
                throw new IOException("Could not load properties file: " + file, ioe);
            }
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
            return value;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported date type for \"" + key + "\": " + value.getClass().getName());
        }
    }

    /**
     * Processes a date {@link Properties properties}.
     * <p>
     * If no value is present and {@code calc} is defined, the calculation starts from {@code ZonedDateTime.now()}.
     * This matches Ant {@code <propertyfile>} behavior. To fail on missing values, do not use {@code calc}
     * without also setting {@code defaultValue} or {@code newValue}.
     *
     * @param p     the {@link Properties properties}
     * @param entry the {@link EntryDate} containing the {@link Properties properties} edits
     * @throws DateTimeException        if a parsing or arithmetic error occurs
     * @throws IllegalArgumentException if no value is configured and no {@code calc} is set
     */
    public static void processDate(@NonNull Properties p, @NonNull EntryDate entry) throws DateTimeException {
        var key = entry.key();
        var effective = resolveValue(p.getProperty(key), entry.defaultValue(), entry.newValue());

        if (effective == null) {
            if (entry.calc() == null) {
                throw new IllegalArgumentException("No value provided for entry: " + key);
            }
            effective = ZonedDateTime.now();
        }

        var patternObj = entry.pattern();
        if (patternObj instanceof String pattern && !pattern.isBlank()) {
            var normalised = normaliseDateValue(effective, key);
            p.setProperty(key, applyOffsetAndFormat(normalised, entry, DateTimeFormatter.ofPattern(pattern)));
        } else {
            p.setProperty(key, String.valueOf(effective));
        }
    }

    /**
     * Processes an integer {@link Properties properties}.
     * <p>
     * If no value is present and {@code calc} is defined, the calculation starts from {@code 0}.
     * This matches Ant {@code <propertyfile>} behavior. To fail on missing values, do not use {@code calc}
     * without also setting {@code defaultValue} or {@code newValue}.
     *
     * @param p     the {@link Properties properties}
     * @param entry the {@link EntryInt} containing the {@link Properties properties} edits
     * @throws IllegalArgumentException if no value is configured and no {@code calc} is set,
     *                                  or the value cannot be parsed as an integer
     */
    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
    public static void processInt(@NonNull Properties p, @NonNull EntryInt entry) {
        var key = entry.key();
        var effective = resolveValue(p.getProperty(key), entry.defaultValue(), entry.newValue());

        if (effective == null) {
            if (entry.calc() == null) {
                throw new IllegalArgumentException("No value provided for entry: " + key);
            }
            effective = 0;
        }

        try {
            var fmt = new DecimalFormat(Objects.toString(entry.pattern(), ""));
            var intValue = fmt.parse(String.valueOf(effective)).intValue();

            if (entry.calc() != null) {
                intValue = entry.calc().apply(intValue);
            }

            p.setProperty(key, fmt.format(intValue));
        } catch (NumberFormatException | ParseException e) {
            throw new IllegalArgumentException(
                    "Non-integer value for \"" + key + "\" --> " + e.getMessage(), e);
        }
    }

    /**
     * Processes a string {@link Properties property}.
     *
     * @param p     the {@link Properties property}
     * @param entry the {@link Entry} containing the {@link Properties property} edits
     * @throws IllegalArgumentException if no value is configured or the modify function fails/returns {@code null}
     */
    public static void processString(@NonNull Properties p, @NonNull Entry entry) {
        var key = entry.key();
        var effective = resolveValue(p.getProperty(key), entry.defaultValue(), entry.newValue());
        if (effective == null) {
            throw new IllegalArgumentException("No value provided for entry: " + key);
        }

        var result = String.valueOf(effective);
        if (entry.pattern() != null) {
            result = String.format(String.valueOf(entry.pattern()), result);
        }

        var modify = entry.modify(); // assign to local
        if (modify != null) {
            result = modify.apply(result, entry.modifyValue());
            if (result == null) {
                throw new IllegalArgumentException(
                        "Modify function returned null for key: " + key);
            }
        }
        p.setProperty(key, result);
    }

    /**
     * Resolves the effective value using precedence: {@code newValue} → existing → {@code defaultValue}.
     * {@code null} means the value is missing. Empty string {@code ""} is considered a valid value.
     *
     * @param existing     the current persisted value; may be {@code null} if the key is absent
     * @param defaultValue the fallback used when both {@code existing} and {@code newValue} are {@code null}
     * @param newValue     when non-{@code null}, always takes precedence
     * @return the resolved object; {@code null} if all three arguments are {@code null}
     */
    @Nullable
    public static Object resolveValue(@Nullable String existing, @Nullable Object defaultValue,
                                      @Nullable Object newValue) {
        if (newValue != null) {
            return newValue;
        } else if (existing != null) {
            return existing;
        } else {
            return defaultValue;
        }
    }

    /**
     * Saves a {@link Properties properties} file atomically when possible.
     * <p>
     * The method writes to a temporary file in the same directory, calls {@code fsync}, then performs
     * an atomic move to replace the target. If the filesystem does not support atomic move or the
     * directory does not allow renames, it falls back to a non-atomic overwrite.
     *
     * @param file    the file location
     * @param comment the header comment
     * @param props   the {@link Properties} to save into the file
     * @throws IOException if an IO error occurs while writing the file
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public static void saveProperties(@NonNull File file, String comment, @NonNull Properties props)
            throws IOException {
        ObjectTools.requireNonNull(file, "file");
        ObjectTools.requireNonNull(props, "props");

        try {
            var parent = file.getParentFile();
            var dir = parent != null ? parent.toPath() : Path.of(".");
            Files.createDirectories(dir);

            var tmp = Files.createTempFile(dir, file.getName(), ".tmp");
            try {
                try (var out =
                             Files.newOutputStream(tmp, StandardOpenOption.WRITE, StandardOpenOption.DSYNC)) {
                    props.store(out, comment);
                }
                try {
                    Files.move(tmp, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(tmp);
            }
        } catch (IOException ioe) {
            throw new IOException("Could not save properties file: " + file, ioe);
        }
    }
}