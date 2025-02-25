/*
 * Copyright 2023-2025 the original author or authors.
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
public class Entry extends EntryBase<Entry> {
    private String modifyValue_ = "";
    private BiFunction<String, String, String> modify_;

    /**
     * Creates a new {@link Entry entry}.
     *
     * @param key the required property key
     */
    public Entry(String key) {
        super(key);
    }

    /**
     * Returns the modify function.
     *
     * @return the modify function
     */
    protected BiFunction<String, String, String> modify() {
        return modify_;
    }

    /**
     * Sets the modify function.
     *
     * @param modify the modify function
     */
    public Entry modify(BiFunction<String, String, String> modify) {
        modify_ = modify;
        return this;
    }

    /**
     * Sets the modify function.
     *
     * @param value  the value to perform a modification with
     * @param modify the modify function
     */
    public Entry modify(String value, BiFunction<String, String, String> modify) {
        modifyValue_ = value;
        modify_ = modify;
        return this;
    }

    /**
     * Returns the value to be used in the {@link #modify_} function.
     *
     * @return the modify value
     */
    protected String modifyValue() {
        return modifyValue_;
    }

    /**
     * Sets the new {@link java.util.Properties property} value.
     *
     * @param s The new value
     * @return the entry
     */
    public Entry set(Object s) {
        newValue(s);
        return this;
    }
}
