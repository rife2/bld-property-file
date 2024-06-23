/*
 * Copyright 2023-2024 the original author or authors.
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

import java.util.function.BiFunction;

/**
 * Declares the modifications to be made to a {@link java.util.Properties String-based property}.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @author <a href="https://github.com/gbevin">Geert Bevin</a>
 * @since 1.0
 */
public class Entry extends EntryBase {
    /**
     * Instantiates a new Entry.
     *
     * @param key the key
     */
    public Entry(String key) {
        super(key);
    }

    /**
     * <p>Sets the initial value to set the {@link java.util.Properties property} to, if not already defined.</p>
     *
     * @param defaultValue the default value
     * @return the entry
     */
    @SuppressWarnings("unused")
    public Entry defaultValue(Object defaultValue) {
        setDefaultValue(defaultValue);
        return this;
    }

    /**
     * Sets the {@link Entry entry} up for deletion.
     *
     * @return the entry
     */
    public Entry delete() {
        setDelete();
        return this;
    }

    /**
     * Creates a new {@link Entry entry}.
     *
     * @param modify the modification function
     * @return the entry
     */
    public Entry modify(BiFunction<String, String, String> modify) {
        setModify(modify);
        return this;
    }

    /**
     * Creates a new {@link Entry entry}.
     *
     * @param value  the value to perform a modification with
     * @param modify the modification function
     * @return the entry
     */
    public Entry modify(String value, BiFunction<String, String, String> modify) {
        setModifyValue(value);
        setModify(modify);
        return this;
    }

    /**
     * Sets the new {@link java.util.Properties property} value.
     *
     * @param s The new value
     * @return the entry
     */
    public Entry set(Object s) {
        setNewValue(s);
        return this;
    }
}
