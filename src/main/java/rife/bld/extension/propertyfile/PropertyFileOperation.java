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
import rife.bld.BaseProject;
import rife.bld.extension.tools.TextUtils;
import rife.bld.operations.AbstractOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates or applies edits to a {@link Properties Properties} file.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class PropertyFileOperation extends AbstractOperation<PropertyFileOperation> {

    private static final Logger LOGGER = Logger.getLogger(PropertyFileOperation.class.getName());
    private final List<EntryBase<?>> entries_ = new ArrayList<>();
    private boolean clear_;
    private String comment_ = "";
    private boolean failOnWarning_;
    private File file_;
    private BaseProject project_;

    /**
     * Performs the modification(s) to the {@link java.util.Properties properties} file.
     */
    @Override
    public void execute() throws Exception {
        if (project_ == null) {
            if (LOGGER.isLoggable(Level.SEVERE) && !silent()) {
                LOGGER.log(Level.SEVERE, "A project is required");
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        }

        var commandName = project_.getCurrentCommandName();
        var properties = new Properties();
        var success = true;

        if (file_ == null) {
            warn(commandName, "A properties file must be specified.");
        } else {
            success = PropertyFileUtils.loadProperties(commandName, file_, properties, failOnWarning_, silent());
        }

        if (success) {
            if (clear_) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("All entries will be cleared first.");
                }
                properties.clear();
            }
            for (var entry : entries_) {
                if (entry.key().isBlank()) {
                    warn(commandName, "An entry key must specified.");
                } else {
                    var key = entry.key();
                    Object value = entry.newValue();
                    Object defaultValue = entry.defaultValue();
                    var p = properties.getProperty(key);
                    if (entry.isDelete()) {
                        properties.remove(key);
                    } else if (TextUtils.isBlank(value, defaultValue, p)) {
                        warn(commandName, "An entry must be set or have a default value: " + key);
                    } else {
                        try {
                            if (entry instanceof EntryDate) {
                                PropertyFileUtils.processDate(properties, (EntryDate) entry);
                            } else if (entry instanceof EntryInt) {
                                PropertyFileUtils.processInt(properties, (EntryInt) entry);
                            } else {
                                PropertyFileUtils.processString(properties, (Entry) entry);
                            }
                        } catch (IllegalArgumentException e) {
                            warn(commandName, e.getMessage());
                        }
                    }
                }
            }
        }

        if (success) {
            PropertyFileUtils.saveProperties(file_, comment_, properties);
        }
    }

    /**
     * Marks the operation to clear all existing entries in the target properties file
     * before applying further modifications.
     *
     * @return this instance
     */
    public PropertyFileOperation clear() {
        clear_ = true;
        return this;
    }

    /**
     * Sets the comment to be inserted at the top of the {@link java.util.Properties} file.
     *
     * @param comment the header comment
     * @return this instance
     */
    public PropertyFileOperation comment(String comment) {
        comment_ = comment;
        return this;
    }

    /**
     * Adds an {@link Entry entry} to specify modifications to the {@link java.util.Properties properties}
     * file.
     *
     * @param entry the {@link Entry entry}
     * @return this instance
     */
    public PropertyFileOperation entry(EntryBase<?> entry) {
        entries_.add(entry);
        return this;
    }

    /**
     * Sets the {@link #execute() execution} to return a failure on any warnings.
     *
     * @param failOnWarning if set to {@code true}, the execution will fail on any warnings.
     * @return this instance
     */
    public PropertyFileOperation failOnWarning(boolean failOnWarning) {
        failOnWarning_ = failOnWarning;
        return this;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     * @return this instance
     */
    public PropertyFileOperation file(File file) {
        file_ = file;
        return this;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     * @return this instance
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public PropertyFileOperation file(String file) {
        return file(new File(file));
    }

    /**
     * Retrieves the location of the {@link java.util.Properties} file to be edited.
     *
     * @return the properties file
     */
    public File file() {
        return file_;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     * @return this instance
     */
    public PropertyFileOperation file(Path file) {
        return file(file.toFile());
    }

    /**
     * Creates a new operation.
     *
     * @param project the project
     * @return this instance
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public PropertyFileOperation fromProject(BaseProject project) {
        project_ = project;
        return this;
    }

    /**
     * Logs a warning.
     *
     * @param command The command name
     * @param message the message log
     * @throws ExitStatusException if a {@link Level#SEVERE} exception occurs
     */
    private void warn(String command, String message) throws ExitStatusException {
        PropertyFileUtils.warn(LOGGER, command, message, failOnWarning_, silent());
    }
}
