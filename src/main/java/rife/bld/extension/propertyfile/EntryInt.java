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

import java.util.function.IntFunction;

/**
 * Declares the modifications to be made to an {@link java.util.Properties Integer-based property}.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class EntryInt extends EntryBase {
    /**
     * Creates a new date {@link Entry entry}.
     *
     * @param key the required property key
     */
    public EntryInt(String key) {
        super(key);
    }

    /**
     * Creates a new {@link EntryInt entry}.
     *
     * @param calc the calculation function.
     * @return this instance
     */
    public EntryInt calc(IntFunction<Integer> calc) {
        setCalc(calc);
        return this;
    }

    /**
     * Sets the initial value to set the {@link java.util.Properties property} to, if not already defined.
     *
     * @param defaultValue the default value
     * @return this instance
     */
    @SuppressWarnings("unused")
    public EntryInt defaultValue(Object defaultValue) {
        setDefaultValue(defaultValue);
        return this;
    }

    /**
     * Sets the {@link EntryInt entry} up for deletion.
     *
     * @return this instance
     */
    public EntryInt delete() {
        setDelete();
        return this;
    }

    /**
     * Sets the new {@link java.util.Properties property} value to an integer.
     *
     * @param i The integer to set the value to
     * @return this instance
     */
    public EntryInt set(int i) {
        setNewValue(i);
        return this;
    }
}
