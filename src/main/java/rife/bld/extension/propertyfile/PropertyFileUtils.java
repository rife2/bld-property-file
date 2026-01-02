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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of common methods used in this project.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class PropertyFileUtils {
    private static final Logger LOGGER = Logger.getLogger(PropertyFileUtils.class.getName());

    private PropertyFileUtils() {
        // no-op
    }

    /**
     * Returns the new value, value or default value depending on which is specified.
     *
     * @param value        the value
     * @param defaultValue the default value
     * @param newValue     the new value
     * @return the object
     */
    public static Object currentValue(String value, Object defaultValue, Object newValue) {
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
     * @param command the issuing command
     * @param file    the file location
     * @param p       the {@link Properties properties} to load into.
     * @return the boolean
     * @throws ExitStatusException if an error occurred
     */
    public static boolean loadProperties(String command, File file, Properties p, boolean silent)
            throws ExitStatusException {
        boolean success = true;
        if (file != null) {
            if (file.exists()) {
                try (var propStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
                    p.load(propStream);
                } catch (IOException ioe) {
                    warn(command, "Could not load properties file: " + ioe.getMessage(), true, silent);
                    success = false;
                }
            }
        } else {
            warn(command, "Please specify the properties file location.", true, silent);
            success = false;
        }
        return success;
    }

    private static String objectToString(Object o) {
        if (o == null) {
            return "";
        } else {
            return String.valueOf(o);
        }
    }

    /**
     * Processes a date {@link Properties property}.
     *
     * @param p     the {@link Properties property}
     * @param entry the {@link Entry} containing the {@link Properties property} edits
     * @throws DateTimeException if a parsing error occurs
     */
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    @SuppressFBWarnings({"DRE_DECLARED_RUNTIME_EXCEPTION", "ITC_INHERITANCE_TYPE_CHECKING"})
    public static void processDate(Properties p, EntryDate entry) throws IllegalArgumentException {
        var currentValue = currentValue(null, entry.defaultValue(), entry.newValue());
        var pattern = objectToString(entry.pattern());

        var dateValue = String.valueOf(currentValue);
        if (pattern != null && !pattern.isBlank()) {
            var offset = 0;

            if (entry.calc() != null) {
                offset = entry.calc().apply(offset);
            }

            var dtf = DateTimeFormatter.ofPattern(pattern);
            var unit = entry.unit();

            try {
                if (currentValue instanceof String) {
                    if ("now".equalsIgnoreCase((String) currentValue)) {
                        currentValue = ZonedDateTime.now();
                    } else {
                        throw new DateTimeException("Excepted: Calendar, Date or java.time.");
                    }
                } else if (currentValue instanceof LocalDateTime) {
                    currentValue = ((LocalDateTime) currentValue).atZone(ZoneId.systemDefault());
                } else if (currentValue instanceof Date) {
                    currentValue = ((Date) currentValue).toInstant().atZone(ZoneId.systemDefault());
                } else if (currentValue instanceof Calendar) {
                    currentValue = ((Calendar) currentValue).toInstant().atZone(ZoneId.systemDefault());
                } else if (currentValue instanceof Instant) {
                    currentValue = ((Instant) currentValue).atZone(ZoneId.systemDefault());
                }

                if (currentValue instanceof LocalDate) {
                    if (offset != 0) {
                        if (unit == EntryDate.Units.DAY) {
                            currentValue = ((LocalDate) currentValue).plusDays(offset);
                        } else if (unit == EntryDate.Units.MONTH) {
                            currentValue = ((LocalDate) currentValue).plusMonths(offset);
                        } else if (unit == EntryDate.Units.WEEK) {
                            currentValue = ((LocalDate) currentValue).plusWeeks(offset);
                        } else if (unit == EntryDate.Units.YEAR) {
                            currentValue = ((LocalDate) currentValue).plusYears(offset);
                        }
                    }
                    dateValue = dtf.format((LocalDate) currentValue);
                } else if (currentValue instanceof LocalTime) {
                    if (offset != 0) {
                        if (unit == EntryDate.Units.SECOND) {
                            currentValue = ((LocalTime) currentValue).plusSeconds(offset);
                        } else if (unit == EntryDate.Units.MINUTE) {
                            currentValue = ((LocalTime) currentValue).plusMinutes(offset);
                        } else if (unit == EntryDate.Units.HOUR) {
                            currentValue = ((LocalTime) currentValue).plusHours(offset);
                        }
                    }
                    dateValue = dtf.format((LocalTime) currentValue);
                } else if (currentValue instanceof ZonedDateTime) {
                    if (offset != 0) {
                        if (unit == EntryDate.Units.DAY) {
                            currentValue = ((ZonedDateTime) currentValue).plusDays(offset);
                        } else if (unit == EntryDate.Units.MONTH) {
                            currentValue = ((ZonedDateTime) currentValue).plusMonths(offset);
                        } else if (unit == EntryDate.Units.WEEK) {
                            currentValue = ((ZonedDateTime) currentValue).plusWeeks(offset);
                        } else if (unit == EntryDate.Units.YEAR) {
                            currentValue = ((ZonedDateTime) currentValue).plusYears(offset);
                        } else if (unit == EntryDate.Units.SECOND) {
                            currentValue = ((ZonedDateTime) currentValue).plusSeconds(offset);
                        } else if (unit == EntryDate.Units.MINUTE) {
                            currentValue = ((ZonedDateTime) currentValue).plusMinutes(offset);
                        } else if (unit == EntryDate.Units.HOUR) {
                            currentValue = ((ZonedDateTime) currentValue).plusHours(offset);
                        }
                    }
                    dateValue = dtf.format((ZonedDateTime) currentValue);
                }
            } catch (DateTimeException dte) {
                throw new IllegalArgumentException(
                        "Non-date value for \"" + entry.key() + "\" --> " + dte.getMessage(), dte);
            }
        }
        p.setProperty(entry.key(), dateValue);
    }

    /**
     * Processes an integer {@link Properties property}.
     *
     * @param p     the {@link Properties property}
     * @param entry the {@link Entry} containing the {@link Properties property} edits
     * @throws NumberFormatException if a parsing error occurs
     */
    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
    public static void processInt(Properties p, EntryInt entry) throws IllegalArgumentException {
        int intValue = 0;
        try {
            var fmt = new DecimalFormat(objectToString(entry.pattern()));
            var currentValue = currentValue(p.getProperty(entry.key()), entry.defaultValue(), entry.newValue());

            if (currentValue != null) {
                intValue = fmt.parse(String.valueOf(currentValue)).intValue();
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
    public static void processString(Properties p, Entry entry) {
        var currentValue = currentValue(p.getProperty(entry.key()), entry.defaultValue(), entry.newValue());

        p.setProperty(entry.key(), String.format(String.valueOf(currentValue), entry.pattern()));

        if (entry.modify() != null && entry.modifyValue() != null) {
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
     * @throws IOException the io exception
     */
    public static void saveProperties(File file, String comment, Properties p) throws IOException {
        try (var output = Files.newOutputStream(file.toPath())) {
            p.store(output, comment);
        } catch (IOException ioe) {
            throw new IOException("An IO error occurred while saving the Properties file: " + file, ioe);
        }
    }

    /**
     * Logs a warning.
     *
     * @param command       The command name
     * @param message       the message log
     * @param failOnWarning logs and throws exception if set to {@code true}
     * @throws ExitStatusException if a {@link Level#SEVERE} exception occurs
     */
    static void warn(String command, String message, boolean failOnWarning, boolean silent)
            throws ExitStatusException {
        if (failOnWarning) {
            if (LOGGER.isLoggable(Level.SEVERE) && !silent) {
                LOGGER.log(Level.SEVERE, "[{0}] {1}", new String[]{command, message});
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        } else {
            if (LOGGER.isLoggable(Level.WARNING) && !silent) {
                LOGGER.log(Level.WARNING, "[{0}] {1}", new String[]{command, message});
            }
        }
    }
}
