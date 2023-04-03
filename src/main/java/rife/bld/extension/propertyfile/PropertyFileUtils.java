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

import rife.bld.extension.propertyfile.Entry.Operations;
import rife.bld.extension.propertyfile.Entry.Units;
import rife.tools.Localization;

import javax.imageio.IIOException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Collection of utility-type methods commonly used in this project.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class PropertyFileUtils {
    private final static Logger LOGGER = Logger.getLogger(PropertyFileUtils.class.getName());

    private final static Map<Units, Integer> calendarFields =
            Map.of(Units.MILLISECOND, Calendar.MILLISECOND,
                    Units.SECOND, Calendar.SECOND,
                    Units.MINUTE, Calendar.MINUTE,
                    Units.HOUR, Calendar.HOUR_OF_DAY,
                    Units.DAY, Calendar.DATE,
                    Units.WEEK, Calendar.WEEK_OF_YEAR,
                    Units.MONTH, Calendar.MONTH,
                    Units.YEAR, Calendar.YEAR);

    private PropertyFileUtils() {
        // no-op
    }

    /**
     * Processes a date {@link Properties property}.
     *
     * @param command the issuing command
     * @param p       the {@link Properties property}
     * @param entry   the {@link Entry} containing the {@link Properties property} edits
     * @return {@code true} if successful
     */
    public static boolean processDate(String command, Properties p, Entry entry, boolean failOnWarning) {
        var success = true;
        var cal = Calendar.getInstance();
        var value = PropertyFileUtils.currentValue(p.getProperty(entry.getKey()), entry.getValue(),
                entry.getDefaultValue(), entry.getOperation());

        var pattern = entry.getPattern();
        SimpleDateFormat fmt;
        if (pattern.isBlank()) {
            fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Localization.getLocale());
        } else {
            fmt = new SimpleDateFormat(entry.getPattern(), Localization.getLocale());
        }
        if ("now".equalsIgnoreCase(value) || value.isBlank()) {
            cal.setTime(new Date());
        } else {
            try {
                cal.setTime(fmt.parse(value));
            } catch (ParseException pe) {
                warn(command, "Non-date value for \"" + entry.getKey() + "\" --> " + pe.getMessage(),
                        pe, failOnWarning);
                success = false;
            }
        }

        if (entry.getOperation() != Entry.Operations.SET) {
            var offset = 0;

            try {
                offset = Integer.parseInt(entry.getValue());
                if (entry.getOperation() == Entry.Operations.SUBTRACT) {
                    offset *= -1;
                }
            } catch (NumberFormatException nfe) {
                warn(command, "Non-date value for \"" + entry.getKey() + "\" --> " + nfe.getMessage(), nfe,
                        failOnWarning);
                success = false;
            }

            //noinspection MagicConstant
            cal.add(calendarFields.getOrDefault(entry.getUnit(), Calendar.DATE), offset);
        }

        p.setProperty(entry.getKey(), fmt.format(cal.getTime()));

        return success;
    }

    /**
     * Return the current value, new value or default value based on the specified {@link Operations operation}.
     *
     * @param value        the value
     * @param newValue     the new value
     * @param defaultValue the default value
     * @param operation    the {@link Operations operation}
     * @return the current value
     */
    public static String currentValue(String value, String newValue, String defaultValue, Operations operation) {
        String result = null;

        if (operation == Entry.Operations.SET) {
            if (newValue != null && defaultValue == null) {
                result = newValue;
            }
            if (defaultValue != null) {
                if (newValue == null && value != null) {
                    result = value;
                }

                if (newValue == null && value == null) {
                    result = defaultValue;
                }

                if (newValue != null && value != null) {
                    result = newValue;
                }

                if (newValue != null && value == null) {
                    result = defaultValue;
                }
            }
        } else {
            if (value == null) {
                result = defaultValue;
            } else {
                result = value;
            }
        }

        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Processes an integer {@link Properties property}.
     *
     * @param command the issuing command
     * @param p       the {@link Properties property}
     * @param entry   the {@link Entry} containing the {@link Properties property} edits
     * @return {@code true} if successful
     */
    public static boolean processInt(String command, Properties p, Entry entry, boolean failOnWarning) {
        var success = true;
        int intValue;
        try {
            var fmt = new DecimalFormat(entry.getPattern());
            var value = PropertyFileUtils.currentValue(p.getProperty(entry.getKey()), entry.getValue(),
                    entry.getDefaultValue(), entry.getOperation());

            if (value.isBlank()) {
                intValue = fmt.parse("0").intValue();
            } else {
                intValue = fmt.parse(value).intValue();
            }

            if (entry.getCalc() != null) {
                intValue = entry.getCalc().apply(intValue);
            }
            else if (entry.getOperation() != Entry.Operations.SET) {
                var opValue = 1;
                if (entry.getValue() != null) {
                    opValue = fmt.parse(entry.getValue()).intValue();
                }
                if (entry.getOperation() == Entry.Operations.ADD) {
                    intValue += opValue;
                } else if (entry.getOperation() == Entry.Operations.SUBTRACT) {
                    intValue -= opValue;
                }
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
     * @return {@code true} if successful
     */
    public static boolean processString(Properties p, Entry entry) {
        var value = PropertyFileUtils.currentValue(p.getProperty(entry.getKey()), entry.getValue(),
                entry.getDefaultValue(), entry.getOperation());

        if (entry.getModify() != null) {
            p.setProperty(entry.getKey(), entry.getModify().apply(p.getProperty(entry.getKey()), entry.getValue()));
        } else if (entry.getOperation() == Entry.Operations.SET) {
            p.setProperty(entry.getKey(), value);
        } else if (entry.getOperation() == Entry.Operations.ADD) {
            if (entry.getValue() != null) {
                p.setProperty(entry.getKey(), p.getProperty(entry.getKey()) + entry.getValue());
            }
        }

        return true;
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
     * @param failOnWarning skips logging the exception if set to {@code false}
     */
    static void warn(String command, String message, Exception e, boolean failOnWarning) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            if (failOnWarning) {
                LOGGER.log(Level.WARNING, '[' + command + "] " + message, e);
            } else {
                LOGGER.warning('[' + command + "] " + message);
            }
        }
    }

    /**
     * Loads a {@link Properties properties} file.
     *
     * @param command the issuing command
     * @param file    the file location.
     * @param p       the {@link Properties properties} to load into.
     * @return {@code true} if successful
     */
    public static boolean loadProperties(String command, File file, Properties p) {
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
}
