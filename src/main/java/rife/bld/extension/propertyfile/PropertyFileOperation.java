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

import rife.bld.Project;
import rife.bld.operations.AbstractOperation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Creates or applies edits to a {@link Properties Properties} file.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class PropertyFileOperation extends AbstractOperation<PropertyFileOperation> {
    private final List<Entry> entries = new ArrayList<>();
    private final Project project;
    private File file;
    private String comment = "";
    private boolean failOnWarning;

    public PropertyFileOperation(Project project) {
        this.project = project;
    }

    /**
     * Adds an {@link Entry entry} to specify modifications to the {@link java.util.Properties properties}
     * file.
     *
     * @param entry the {@link Entry entry}
     */
    @SuppressWarnings("unused")
    public PropertyFileOperation entry(Entry entry) {
        entries.add(entry);
        return this;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     */
    @SuppressWarnings("unused")
    public PropertyFileOperation file(String file) {
        this.file = new File(file);
        return this;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     */
    @SuppressWarnings("unused")
    public PropertyFileOperation file(File file) {
        this.file = file;
        return this;
    }

    /**
     * Sets the command to return a failure on any warnings.
     *
     * @param failOnWarning if set to {@code true}, the task will fail on any warnings.
     */
    @SuppressWarnings("unused")
    public PropertyFileOperation failOnWarning(boolean failOnWarning) {
        this.failOnWarning = failOnWarning;
        return this;
    }

    /**
     * Sets the comment to be inserted at the top of the {@link java.util.Properties} file.
     *
     * @param comment the header comment
     */
    @SuppressWarnings("unused")
    public PropertyFileOperation comment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Performs the modification(s) to the {@link java.util.Properties properties} file.
     */
    @Override
    public void execute() throws Exception {
        if (project == null) {
            throw new IOException("A project must be specified.");
        }
        if (file == null) {
            throw new IOException("A properties file location must be specified.");
        }
        var commandName = project.getCurrentCommandName();
        var success = false;
        var properties = new Properties();
        success = PropertyFileUtils.loadProperties(commandName, file, properties);
        if (success) {
            for (var entry : entries) {
                if (entry.getKey().isBlank()) {
                    PropertyFileUtils.warn(commandName, "At least one entry key must specified.");
                    success = false;
                } else {
                    var key = entry.getKey();
                    var value = entry.getNewValue();
                    var defaultValue = entry.getDefaultValue();
                    if (entry.isDelete()) {
                        properties.remove(key);
                    } else if ((value == null || value.isBlank()) && (defaultValue == null || defaultValue.isBlank())) {
                        PropertyFileUtils.warn(commandName, "An entry must be set or have a default value: " + key);
                        success = false;
                    } else {
                        switch (entry.getType()) {
                            case DATE ->
                                    success = PropertyFileUtils.processDate(commandName, properties, entry, failOnWarning);
                            case INT ->
                                    success = PropertyFileUtils.processInt(commandName, properties, entry, failOnWarning);
                            default -> success = PropertyFileUtils.processString(properties, entry);
                        }
                    }
                }
            }
        }
        if (failOnWarning && !success) {
            throw new RuntimeException("Properties file configuration failed: " + file);
        } else if (success) {
            PropertyFileUtils.saveProperties(file, comment, properties);
        }
    }
}
