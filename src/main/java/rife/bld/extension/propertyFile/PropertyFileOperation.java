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

package rife.bld.extension.propertyFile;

import rife.bld.Project;
import rife.bld.extension.propertyFile.Entry.Operations;
import rife.bld.extension.propertyFile.Entry.Types;
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
    private File file = null;
    private String comment = "";
    private boolean failOnWarning = false;

    public PropertyFileOperation(Project project) {
        this.project = project;
    }

    /**
     * Adds an {@link Entry entry} to specify modifications to the {@link java.util.Properties properties}
     * file.
     *
     * @param entry the {@link Entry entry}
     */
    public PropertyFileOperation entry(Entry entry) {
        entries.add(entry);
        return this;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     */
    public PropertyFileOperation file(String file) {
        this.file = new File(file);
        return this;
    }

    /**
     * Sets the location of the {@link java.util.Properties} file to be edited.
     *
     * @param file the file to be edited
     */
    public PropertyFileOperation file(File file) {
        this.file = file;
        return this;
    }

    /**
     * Sets the command to return a failure on any warnings.
     *
     * @param failOnWarning if set to {@code true}, the task will fail on any warnings.
     */
    public PropertyFileOperation failOnWarning(boolean failOnWarning) {
        this.failOnWarning = failOnWarning;
        return this;
    }

    /**
     * Sets the comment to be inserted at the top of the {@link java.util.Properties} file.
     *
     * @param comment the header comment
     */
    public PropertyFileOperation comment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Performs the edits to the {@link java.util.Properties properties} file.
     */
    public void execute() throws Exception {
        if (project == null) {
            throw new IOException("A project must be specified.");
        }
        if (file == null) {
            throw new IOException("A properties file location must be specified.");
        }
        var success = false;
        var properties = new Properties();
        success = PropertyFileUtils.loadProperties(file, properties);
        if (success) {
            for (var entry : entries) {
                if (entry.getKey().isBlank()) {
                    PropertyFileUtils.warn("At least one entry key must specified.");
                    success = false;
                } else {
                    var key = entry.getKey();
                    var value = entry.getValue();
                    var defaultValue = entry.getDefaultValue();
                    if ((value == null || value.isBlank()) && (defaultValue == null || defaultValue.isBlank())
                            && entry.getOperation() != Operations.DELETE) {
                        PropertyFileUtils.warn("An entry value or default must be specified: " + key);
                        success = false;
                    } else if (entry.getType() == Types.STRING && entry.getOperation() == Operations.SUBTRACT) {
                        PropertyFileUtils.warn("Subtraction is not supported for String properties: " + key);
                        success = false;
                    } else if (entry.getOperation() == Operations.DELETE) {
                        properties.remove(key);
                    } else {
                        switch (entry.getType()) {
                            case DATE -> success = PropertyFileUtils.processDate(properties, entry, failOnWarning);
                            case INT -> success = PropertyFileUtils.processInt(properties, entry, failOnWarning);
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
