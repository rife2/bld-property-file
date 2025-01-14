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

import java.util.function.IntFunction;

/**
 * Declares the modifications to be made to a {@link java.util.Properties property}.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @author <a href="https://github.com/gbevin">Geert Bevin</a>
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "PMD.AbstractClassWithoutAbstractMethod"})
public abstract class EntryBase<T> {
    private IntFunction<Integer> calc_;
    private Object defaultValue_;
    private boolean isDelete_;
    private String key_;
    private Object newValue_;
    private Object pattern_;

    /**
     * Creates a new {@link EntryBase entry}.
     *
     * @param key the required property key
     */
    public EntryBase(String key) {
        key_ = key;
    }

    /**
     * Returns the calculation function.
     *
     * @return the calc function
     */
    protected IntFunction<Integer> calc() {
        return calc_;
    }

    /**
     * Sets the calculation function.
     *
     * @param calc the calc function
     */
    public T calc(IntFunction<Integer> calc) {
        calc_ = calc;
        return (T) this;
    }

    /**
     * Returns the default value.
     *
     * @return the default value
     */
    protected Object defaultValue() {
        return defaultValue_;
    }

    /**
     * Sets the initial value to set the {@link java.util.Properties property} to, if not already defined.
     *
     * @param defaultValue the default value
     */
    public T defaultValue(Object defaultValue) {
        defaultValue_ = defaultValue;
        return (T) this;
    }

    /**
     * Indicates that the {@link java.util.Properties property} is to be deleted.
     */
    public T delete() {
        isDelete_ = true;
        return (T) this;
    }

    /**
     * Returns {@code true} if the {@link java.util.Properties property} is to be deleted.
     *
     * @return {@code true} or {@code false}
     */
    protected boolean isDelete() {
        return isDelete_;
    }

    /**
     * Returns the key of the {@link java.util.Properties property}.
     *
     * @return the key
     */
    protected String key() {
        return key_;
    }

    /**
     * Sets the key of the {@link java.util.Properties property}.
     *
     * @param key the {@link java.util.Properties property} key
     * @return this instance
     */
    public T key(String key) {
        key_ = key;
        return (T) this;
    }

    /**
     * Returns the new value to set the {@link java.util.Properties property)} to.
     *
     * @return the new value
     */
    protected Object newValue() {
        return newValue_;
    }

    /**
     * Sets a new value for {@link java.util.Properties property}.
     *
     * @param newValue the new value
     */
    protected void newValue(Object newValue) {
        newValue_ = newValue;
    }

    /**
     * Returns the pattern.
     *
     * @return the pattern
     */
    protected Object pattern() {
        return pattern_;
    }

    /**
     * Sets the {@link java.util.Formatter} pattern.
     *
     * @param pattern the pattern
     */
    public T pattern(Object pattern) {
        pattern_ = pattern;
        return (T) this;
    }
}
