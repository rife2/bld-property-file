/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package rife.bld.extension.propertyfile;

import javax.imageio.IIOException;
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
    private final static Logger LOGGER = Logger.getLogger(PropertyFileUtils.class.getName());

    private PropertyFileUtils() {
        // no-op
    }

    /**
     * Returns the new value, value or default value depending on which is specified.
     *
     * @param value        the value
     * @param newValue     the new value
     * @param defaultValue the default value
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
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public static boolean loadProperties(String command, File file, Properties p) throws Exception {
        boolean success = true;
        if (file != null) {
            if (file.exists()) {
                try (var propStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
                    p.load(propStream);
                } catch (IOException ioe) {
                    warn(command, "Could not load properties file: " + ioe.getMessage(), ioe, true);
                    success = false;
                }
            }
        } else {
            warn(command, "Please specify the properties file location.");
            success = false;
        }
        return success;
    }

    /**
     * Processes a date {@link Properties property}.
     *
     * @param command the issuing command
     * @param p       the {@link Properties property}
     * @param entry   the {@link Entry} containing the {@link Properties property} edits
     */
    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.ExceptionAsFlowControl"})
    public static boolean processDate(String command, Properties p, EntryDate entry, boolean failOnWarning)
            throws Exception {
        var success = true;
        var value = currentValue(null, entry.getDefaultValue(),
                entry.getNewValue());

        var pattern = entry.getPattern();

        String parsedValue = String.valueOf(value);
        if (pattern != null && !pattern.isBlank()) {
            var offset = 0;

            if (entry.getCalc() != null) {
                offset = entry.getCalc().apply(offset);
            }

            var dtf = DateTimeFormatter.ofPattern(pattern);
            var unit = entry.getUnit();

            try {
                if (value instanceof String) {
                    if ("now".equalsIgnoreCase((String) value)) {
                        value = ZonedDateTime.now();
                    } else {
                        throw new DateTimeException("Excepted: Calendar, Date or java.time.");
                    }
                } else if (value instanceof LocalDateTime) {
                    value = ((LocalDateTime) value).atZone(ZoneId.systemDefault());
                } else if (value instanceof Date) {
                    value = ((Date) value).toInstant().atZone(ZoneId.systemDefault());
                } else if (value instanceof Calendar) {
                    value = ((Calendar) value).toInstant().atZone(ZoneId.systemDefault());
                } else if (value instanceof Instant) {
                    value = ((Instant) value).atZone(ZoneId.systemDefault());
                }

                if (value instanceof LocalDate) {
                    if (offset != 0) {
                        if (unit == EntryDate.Units.DAY) {
                            value = ((LocalDate) value).plusDays(offset);
                        } else if (unit == EntryDate.Units.MONTH) {
                            value = ((LocalDate) value).plusMonths(offset);
                        } else if (unit == EntryDate.Units.WEEK) {
                            value = ((LocalDate) value).plusWeeks(offset);
                        } else if (unit == EntryDate.Units.YEAR) {
                            value = ((LocalDate) value).plusYears(offset);
                        }
                    }
                    parsedValue = dtf.format((LocalDate) value);
                } else if (value instanceof LocalTime) {
                    if (offset != 0) {
                        if (unit == EntryDate.Units.SECOND) {
                            value = ((LocalTime) value).plusSeconds(offset);
                        } else if (unit == EntryDate.Units.MINUTE) {
                            value = ((LocalTime) value).plusMinutes(offset);
                        } else if (unit == EntryDate.Units.HOUR) {
                            value = ((LocalTime) value).plusHours(offset);
                        }
                    }
                    parsedValue = dtf.format((LocalTime) value);
                } else if (value instanceof ZonedDateTime) {
                    if (offset != 0) {
                        if (unit == EntryDate.Units.DAY) {
                            value = ((ZonedDateTime) value).plusDays(offset);
                        } else if (unit == EntryDate.Units.MONTH) {
                            value = ((ZonedDateTime) value).plusMonths(offset);
                        } else if (unit == EntryDate.Units.WEEK) {
                            value = ((ZonedDateTime) value).plusWeeks(offset);
                        } else if (unit == EntryDate.Units.YEAR) {
                            value = ((ZonedDateTime) value).plusYears(offset);
                        } else if (unit == EntryDate.Units.SECOND) {
                            value = ((ZonedDateTime) value).plusSeconds(offset);
                        } else if (unit == EntryDate.Units.MINUTE) {
                            value = ((ZonedDateTime) value).plusMinutes(offset);
                        } else if (unit == EntryDate.Units.HOUR) {
                            value = ((ZonedDateTime) value).plusHours(offset);
                        }
                    }
                    parsedValue = dtf.format((ZonedDateTime) value);
                }
            } catch (DateTimeException dte) {
                warn(command, "Non-date value for \"" + entry.getKey() + "\" --> " + dte.getMessage(),
                        dte, failOnWarning);
                success = false;
            }
        }

        if (success) {
            p.setProperty(entry.getKey(), parsedValue);
        }

        return success;
    }

    /**
     * Processes an integer {@link Properties property}.
     *
     * @param command the issuing command
     * @param p       the {@link Properties property}
     * @param entry   the {@link Entry} containing the {@link Properties property} edits
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public static boolean processInt(String command, Properties p, EntryInt entry, boolean failOnWarning)
            throws Exception {
        var success = true;
        int intValue = 0;
        try {
            var fmt = new DecimalFormat(entry.getPattern());
            var value = currentValue(p.getProperty(entry.getKey()), entry.getDefaultValue(),
                    entry.getNewValue());

            if (value != null) {
                intValue = fmt.parse(String.valueOf(value)).intValue();
            }

            if (entry.getCalc() != null) {
                intValue = entry.getCalc().apply(intValue);
            }
            p.setProperty(entry.getKey(), fmt.format(intValue));
        } catch (NumberFormatException | ParseException e) {
            warn(command, "Non-integer value for \"" + entry.getKey() + "\" --> " + e.getMessage(), e,
                    failOnWarning);
            success = false;
        }

        return success;
    }

    /**
     * Processes a string {@link Properties property}.
     *
     * @param p     the {@link Properties property}
     * @param entry the {@link Entry} containing the {@link Properties property} edits
     */
    public static boolean processString(Properties p, Entry entry) {
        var value = currentValue(p.getProperty(entry.getKey()), entry.getDefaultValue(), entry.getNewValue());

        p.setProperty(entry.getKey(), String.valueOf(value));

        if (entry.getModify() != null && entry.getModifyValue() != null) {
            p.setProperty(entry.getKey(), entry.getModify().apply(p.getProperty(entry.getKey()), entry.getModifyValue()));
        }

        return true;
    }

    /**
     * Saves a {@link Properties properties} file.
     *
     * @param file    the file location
     * @param comment the header comment
     * @param p       the {@link Properties} to save into the file
     */
    public static void saveProperties(File file, String comment, Properties p) throws IOException {
        try (var output = Files.newOutputStream(file.toPath())) {
            p.store(output, comment);
        } catch (IIOException ioe) {
            throw new IIOException("An IO error occurred while saving the Properties file: " + file, ioe);
        }
    }

    /**
     * Logs a warning.
     *
     * @param command the issuing command
     * @param message the message to log
     */
    static void warn(String command, String message) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.warning('[' + command + "] " + message);
        }
    }

    /**
     * Logs a warning.
     *
     * @param command       The command name
     * @param message       the message log
     * @param e             the related exception
     * @param failOnWarning logs and throws exception if set to {@code true}
     */
    @SuppressWarnings({"PMD.SignatureDeclareThrowsException"})
    static void warn(String command, String message, Exception e, boolean failOnWarning) throws Exception {
        if (failOnWarning) {
            LOGGER.log(Level.SEVERE, '[' + command + "] " + message, e);
            throw e;
        } else {
            warn(command, message);
        }
    }
}
