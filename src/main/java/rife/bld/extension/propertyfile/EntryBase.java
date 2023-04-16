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

import java.util.function.BiFunction;
import java.util.function.IntFunction;

/**
 * Declares the modifications to be made to a {@link java.util.Properties property}.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @author <a href="https://github.com/gbevin">Geert Bevin</a>
 * @since 1.0
 */
public class EntryBase {
    private IntFunction<Integer> calc;
    private Object defaultValue;
    private boolean isDelete;
    private String key;
    private BiFunction<String, String, String> modify;
    private String modifyValue = "";
    private Object newValue;
    private String pattern = "";
    private EntryDate.Units unit = EntryDate.Units.DAY;

    /**
     * Creates a new {@link EntryBase entry}.
     *
     * @param key the required property key
     */
    public EntryBase(String key) {
        this.key = key;
    }

    /**
     * Returns the calculation function.
     */
    protected IntFunction<Integer> getCalc() {
        return calc;
    }

    /**
     * Sets the calculation function.
     */
    protected void setCalc(IntFunction<Integer> calc) {
        this.calc = calc;
    }

    /**
     * Returns the default value.
     */
    protected Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the initial value to set the {@link java.util.Properties property} to, if not already defined.
     *
     * @param defaultValue the default value
     */
    protected void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the key of the {@link java.util.Properties property}.
     */
    protected String getKey() {
        return key;
    }

    /**
     * Sets the key of the {@link java.util.Properties property}.
     *
     * @param key the {@link java.util.Properties property} key
     */
    protected void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the modify function.
     */
    protected BiFunction<String, String, String> getModify() {
        return modify;
    }

    /**
     * Sets the modify function.
     */
    protected void setModify(BiFunction<String, String, String> modify) {
        this.modify = modify;
    }

    /**
     * Returns the value to be used in the {@link #modify} function.
     */
    protected String getModifyValue() {
        return modifyValue;
    }

    /**
     * Sets the modify value.
     *
     * @param value the modify value.
     */
    protected void setModifyValue(String value) {
        this.modifyValue = value;
    }

    /**
     * Returns the new value to set the {@link java.util.Properties property)} to.
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Sets a new value for {@link java.util.Properties property}.
     *
     * @param newValue the new value
     */
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    /**
     * Returns the pattern.
     */
    protected String getPattern() {
        return pattern;
    }

    /**
     * Sets the {@link java.text.DecimalFormat DecimalFormat} or {@link java.time.format.DateTimeFormatter DateTimeFormatter}
     * pattern to be used with {@link EntryDate} or {@link EntryInt} respectively.
     *
     * @param pattern the pattern
     */
    protected void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns the {@link EntryDate.Units unit}.
     */
    protected EntryDate.Units getUnit() {
        return unit;
    }

    /**
     * Sets the {@link EntryDate.Units unit} value to apply to calculations.
     *
     * @param unit the {@link EntryDate.Units unit}
     */
    protected void setUnit(EntryDate.Units unit) {
        this.unit = unit;
    }

    /**
     * Returns {@code true} if the {@link java.util.Properties property} is to be deleted.
     */
    protected boolean isDelete() {
        return isDelete;
    }

    /**
     * Sets whether the {@link java.util.Properties property} should be deleted.
     */
    protected void setDelete(boolean delete) {
        isDelete = delete;
    }

    /**
     * Sets the key of the {@link java.util.Properties property}.
     *
     * @param key the {@link java.util.Properties property} key
     */
    @SuppressWarnings("unused")
    public EntryBase key(String key) {
        setKey(key);
        return this;
    }

    /**
     * Sets the modify function.
     *
     * @param value the value to perform a modification with
     */
    protected void setModify(String value, BiFunction<String, String, String> modify) {
        this.modifyValue = value;
        this.modify = modify;
    }
}
