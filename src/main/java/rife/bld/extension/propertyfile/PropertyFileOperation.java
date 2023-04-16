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

import rife.bld.BaseProject;
import rife.bld.operations.AbstractOperation;

import java.io.File;
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
    private final List<EntryBase> entries = new ArrayList<>();
    private String comment = "";
    private boolean failOnWarning;
    private File file;
    private BaseProject project;

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
     * Adds an {@link Entry entry} to specify modifications to the {@link java.util.Properties properties}
     * file.
     *
     * @param entry the {@link Entry entry}
     */
    @SuppressWarnings("unused")
    public PropertyFileOperation entry(EntryBase entry) {
        entries.add(entry);
        return this;
    }

    /**
     * Performs the modification(s) to the {@link java.util.Properties properties} file.
     */
    @Override
    public void execute() throws Exception {
        var commandName = project.getCurrentCommandName();
        var properties = new Properties();
        var success = false;

        if (file == null) {
            PropertyFileUtils.warn(commandName, "A properties file must be specified.");
        } else {
            success = PropertyFileUtils.loadProperties(commandName, file, properties);
        }

        if (success) {
            for (var entry : entries) {
                if (entry.getKey().isBlank()) {
                    PropertyFileUtils.warn(commandName, "An entry key must specified.");
                } else {
                    var key = entry.getKey();
                    Object value = entry.getNewValue();
                    Object defaultValue = entry.getDefaultValue();
                    var p = properties.getProperty(key);
                    if (entry.isDelete()) {
                        properties.remove(key);
                    } else if ((value == null || String.valueOf(value).isBlank())
                            && (defaultValue == null || String.valueOf(defaultValue).isBlank())
                            && (p == null || p.isBlank())) {
                        PropertyFileUtils.warn(commandName, "An entry must be set or have a default value: " + key);
                    } else {
                        if (entry instanceof EntryDate) {
                            success = PropertyFileUtils.processDate(commandName, properties, (EntryDate) entry, failOnWarning);
                        } else if (entry instanceof EntryInt) {
                            success = PropertyFileUtils.processInt(commandName, properties, (EntryInt) entry, failOnWarning);
                        } else {
                            success = PropertyFileUtils.processString(properties, (Entry) entry);
                        }
                    }
                }
            }
        }

        if (success) {
            PropertyFileUtils.saveProperties(file, comment, properties);
        }
    }

    /**
     * Sets the {@link #execute() execution} to return a failure on any warnings.
     *
     * @param failOnWarning if set to {@code true}, the execution will fail on any warnings.
     */
    @SuppressWarnings("unused")
    public PropertyFileOperation failOnWarning(boolean failOnWarning) {
        this.failOnWarning = failOnWarning;
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
     * Creates a new operation.
     */
    public PropertyFileOperation fromProject(BaseProject project) {
        this.project = project;
        return this;
    }
}
