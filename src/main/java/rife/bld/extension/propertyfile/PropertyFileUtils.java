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

    private final static Map<Units, Integer> CALENDAR_FIELDS =
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
        String value = PropertyFileUtils.currentValue(p.getProperty(entry.getKey()), entry.getDefaultValue(),
                entry.getNewValue());

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

        var offset = 0;

        if (entry.getCalc() != null) {
            offset = entry.getCalc().apply(offset);
        }

        //noinspection MagicConstant
        cal.add(CALENDAR_FIELDS.getOrDefault(entry.getUnit(), Calendar.DATE), offset);

        p.setProperty(entry.getKey(), fmt.format(cal.getTime()));

        return success;
    }

    /**
     * Returns the new value, value or default value depending on which is specified.
     *
     * @param value        the value
     * @param newValue     the new value
     * @param defaultValue the default value
     * @return the current value
     */
    public static String currentValue(String value, String defaultValue, String newValue) {
        if (newValue != null) {
            return newValue;
        } else if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
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
        int intValue = 0;
        try {
            var fmt = new DecimalFormat(entry.getPattern());
            String value = PropertyFileUtils.currentValue(p.getProperty(entry.getKey()), entry.getDefaultValue(),
                    entry.getNewValue());

            if (value != null) {
                intValue = fmt.parse(value).intValue();
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
     * @return {@code true} if successful
     */
    public static boolean processString(Properties p, Entry entry) {
        var value = PropertyFileUtils.currentValue(p.getProperty(entry.getKey()), entry.getDefaultValue(),
                entry.getNewValue());

        p.setProperty(entry.getKey(), value);

        if (entry.getModify() != null && entry.getModifyValue() != null) {
            p.setProperty(entry.getKey(), entry.getModify().apply(p.getProperty(entry.getKey()), entry.getModifyValue()));
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
