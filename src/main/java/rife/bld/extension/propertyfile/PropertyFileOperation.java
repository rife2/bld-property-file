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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import rife.bld.BaseProject;
import rife.bld.extension.tools.ObjectTools;
import rife.bld.extension.tools.TextTools;
import rife.bld.operations.AbstractOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
@NullMarked
public class PropertyFileOperation extends AbstractOperation<PropertyFileOperation> {

    private static final Logger logger = Logger.getLogger(PropertyFileOperation.class.getName());
    private final List<EntryBase<?>> entries_ = new ArrayList<>();
    private boolean clear_;
    private String comment_ = "";
    private boolean failOnWarning_;
    private @Nullable File file_;
    private @Nullable BaseProject project_;

    /**
     * Performs the modification(s) to the {@link java.util.Properties properties} file.
     */
    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE")
    public void execute() throws Exception {
        ObjectTools.requireNonNull(project_, "project");
        ObjectTools.requireNonNull(file_, "properties file");

        var properties = new Properties();

        try {
            PropertyFileUtils.loadProperties(file_, properties);
        } catch (IOException | IllegalArgumentException e) {
            if (logger.isLoggable(Level.SEVERE) && !silent()) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        }

        if (clear_) {
            if (logger.isLoggable(Level.WARNING) && !silent()) {
                logger.warning("All entries will be cleared first.");
            }
            properties.clear();
        }

        for (var entry : entries_) {
            var key = entry.key();
            if (TextTools.isBlank(key)) {
                warn("An entry key must be specified.");
            } else if (entry.isDelete()) {
                properties.remove(key);
            } else {
                Object value = entry.newValue();
                Object defaultValue = entry.defaultValue();
                var existing = properties.getProperty(key); // null if key not present

                // Precedence: newValue -> defaultValue -> existing property -> warn
                var effectiveValue = value != null ? value
                        : defaultValue != null ? defaultValue
                        : existing;

                if (effectiveValue == null) {
                    warn("No value provided for entry: " + key);
                } else {
                    try {
                        // Use pattern-matching instanceof (Java 16+) to avoid raw casts
                        if (entry instanceof EntryDate ed) {
                            PropertyFileUtils.processDate(properties, ed);
                        } else if (entry instanceof EntryInt ei) {
                            PropertyFileUtils.processInt(properties, ei);
                        } else if (entry instanceof Entry e) {
                            PropertyFileUtils.processString(properties, e);
                        }
                    } catch (IllegalArgumentException e) {
                        warn(e.getLocalizedMessage(), e);
                    }
                }
            }
        }

        PropertyFileUtils.saveProperties(file_, comment_, properties);
    }

    /**
     * Marks the operation to clear all existing entries in the target properties file
     * before applying further modifications.
     *
     * @return this instance
     * @see #clear(boolean)
     */
    public PropertyFileOperation clear() {
        clear_ = true;
        return this;
    }

    /**
     * Sets whether all existing entries in the target properties file should be cleared
     * before applying further modifications.
     *
     * @param clear if set to {@code true}, all existing entries will be cleared first
     * @return this instance
     * @see #clear()
     * @see #isClear()
     */
    public PropertyFileOperation clear(boolean clear) {
        clear_ = clear;
        return this;
    }

    /**
     * Sets the comment to be inserted at the top of the {@link java.util.Properties} file.
     *
     * @param comment the header comment; must not be {@code null}
     * @return this instance
     * @throws NullPointerException if {@code comment} is {@code null}
     */
    public PropertyFileOperation comment(String comment) {
        comment_ = ObjectTools.requireNonNull(comment, "comment");
        return this;
    }

    /**
     * Retrieves an unmodifiable view of the configured entries.
     *
     * @return the list of entries
     */
    public List<EntryBase<?>> entries() {
        return Collections.unmodifiableList(entries_);
    }

    /**
     * Adds an {@link Entry entry} to specify modifications to the {@link java.util.Properties properties}
     * file.
     *
     * @param entry the {@link Entry entry}
     * @return this instance
     * @throws NullPointerException if {@code entry} is {@code null}
     */
    public PropertyFileOperation entry(EntryBase<?> entry) {
        entries_.add(ObjectTools.requireNonNull(entry, "entry"));
        return this;
    }

    /**
     * Sets the {@link #execute() execution} to return a failure on any warnings.
     *
     * @param failOnWarning if set to {@code true}, the execution will fail on any warnings.
     * @return this instance
     * @see #isFailOnWarning()
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
     * @throws NullPointerException if {@code file} is {@code null}
     */
    public PropertyFileOperation file(File file) {
        file_ = ObjectTools.requireNonNull(file, "file");

        return this;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     * @return this instance
     * @throws NullPointerException     if {@code file} is {@code null}
     * @throws IllegalArgumentException if {@code file} is empty
     */
    public PropertyFileOperation file(String file) {
        ObjectTools.requireNotEmpty(file, "file");
        return file(new File(file));
    }

    /**
     * Retrieves the location of the {@link java.util.Properties} file to be edited.
     *
     * @return the properties file or {@code null} if not set
     */
    @Nullable
    public File file() {
        return file_;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     * @return this instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    public PropertyFileOperation file(Path file) {
        ObjectTools.requireNonNull(file, "file");
        return file(file.toFile());
    }

    /**
     * Creates a new operation.
     *
     * @param project the project
     * @return this instance
     * @throws NullPointerException if {@code project} is {@code null}
     */
    public PropertyFileOperation fromProject(BaseProject project) {
        project_ = ObjectTools.requireNonNull(project, "fromProject");
        return this;
    }

    /**
     * Indicates whether all existing entries will be cleared before modifications are applied.
     *
     * @return {@code true} if existing entries will be cleared; {@code false} otherwise
     * @see #clear(boolean)
     */
    public boolean isClear() {
        return clear_;
    }

    /**
     * Indicates whether the {@link #execute() execution} will return a failure on any warnings.
     *
     * @return {@code true} if the execution will fail on warnings; {@code false} otherwise
     * @see #failOnWarning(boolean)
     */
    public boolean isFailOnWarning() {
        return failOnWarning_;
    }

    private void warn(String message, @Nullable Throwable cause) throws ExitStatusException {
        String fullMessage;

        if (project_ != null && project_.getCurrentCommandName() != null) {
            fullMessage = "[" + project_.getCurrentCommandName() + "] " + message;
        } else {
            fullMessage = message;
        }

        if (failOnWarning_) {
            if (logger.isLoggable(Level.SEVERE) && !silent()) {
                if (cause == null) {
                    logger.log(Level.SEVERE, fullMessage);
                } else {
                    logger.log(Level.SEVERE, fullMessage, cause);
                }
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        } else {
            if (logger.isLoggable(Level.WARNING) && !silent()) {
                if (cause == null) {
                    logger.log(Level.WARNING, fullMessage);
                } else {
                    logger.log(Level.WARNING, fullMessage, cause);
                }
            }
        }
    }

    private void warn(String message) throws ExitStatusException {
        warn(message, null);
    }
}
