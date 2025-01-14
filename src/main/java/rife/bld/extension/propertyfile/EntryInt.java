/*
 * Copyright 2023-Copyright $today.yearamp;#36;today.year the original author or authors.
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

/**
 * Declares the modifications to be made to an {@link java.util.Properties Integer-based property}.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class EntryInt extends EntryBase<EntryInt> {
    /**
     * Creates a new {@link EntryInt entry}.
     *
     * @param key the required property key
     */
    public EntryInt(String key) {
        super(key);
    }

    /**
     * Sets the {@link java.text.DecimalFormat DecimalFormat} pattern.
     *
     * @param pattern the pattern
     */
    public EntryInt pattern(String pattern) {
        super.pattern(pattern);
        return this;
    }

    /**
     * Sets the new {@link java.util.Properties property} value to an integer.
     *
     * @param i The integer to set the value to
     * @return this instance
     */
    public EntryInt set(int i) {
        newValue(i);
        return this;
    }
}
