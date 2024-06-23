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
import java.util.function.IntFunction;

/**
 * Declares the modifications to be made to a {@link java.util.Properties property}.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @author <a href="https://github.com/gbevin">Geert Bevin</a>
 * @since 1.0
 */
@SuppressWarnings("PMD.DataClass")
public class EntryBase {
    private IntFunction<Integer> calc_;
    private Object defaultValue_;
    private boolean isDelete_;
    private String key_;
    private String modifyValue_ = "";
    private BiFunction<String, String, String> modify_;
    private Object newValue_;
    private String pattern_ = "";
    private EntryDate.Units unit_ = EntryDate.Units.DAY;

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
    protected IntFunction<Integer> getCalc() {
        return calc_;
    }

    /**
     * Returns the default value.
     *
     * @return the default value
     */
    protected Object getDefaultValue() {
        return defaultValue_;
    }

    /**
     * Returns the key of the {@link java.util.Properties property}.
     *
     * @return the key
     */
    protected String getKey() {
        return key_;
    }

    /**
     * Returns the modify function.
     *
     * @return the modify function
     */
    protected BiFunction<String, String, String> getModify() {
        return modify_;
    }

    /**
     * Returns the value to be used in the {@link #modify_} function.
     *
     * @return the modify value
     */
    protected String getModifyValue() {
        return modifyValue_;
    }

    /**
     * Returns the new value to set the {@link java.util.Properties property)} to.
     *
     * @return the new value
     */
    public Object getNewValue() {
        return newValue_;
    }

    /**
     * Returns the pattern.
     *
     * @return the pattern
     */
    protected String getPattern() {
        return pattern_;
    }

    /**
     * Returns the {@link EntryDate.Units unit}.
     *
     * @return the unit
     */
    protected EntryDate.Units getUnit() {
        return unit_;
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
     * Sets the key of the {@link java.util.Properties property}.
     *
     * @param key the {@link java.util.Properties property} key
     * @return this instance
     */
    @SuppressWarnings("unused")
    public EntryBase key(String key) {
        key_ = key;
        return this;
    }

    /**
     * Sets the calculation function.
     *
     * @param calc the calc function
     */
    protected void setCalc(IntFunction<Integer> calc) {
        calc_ = calc;
    }

    /**
     * Sets the initial value to set the {@link java.util.Properties property} to, if not already defined.
     *
     * @param defaultValue the default value
     */
    protected void setDefaultValue(Object defaultValue) {
        defaultValue_ = defaultValue;
    }

    /**
     * Sets the {@link java.util.Properties property} to be deleted.
     */
    protected void setDelete() {
        isDelete_ = true;
    }

    /**
     * Sets the key of the {@link java.util.Properties property}.
     *
     * @param key the {@link java.util.Properties property} key
     */
    protected void setKey(String key) {
        key_ = key;
    }

    /**
     * Sets the modify function.
     *
     * @param modify the modify function
     */
    protected void setModify(BiFunction<String, String, String> modify) {
        modify_ = modify;
    }

    /**
     * Sets the modify function.
     *
     * @param value  the value to perform a modification with
     * @param modify the modify function
     */
    protected void setModify(String value, BiFunction<String, String, String> modify) {
        modifyValue_ = value;
        modify_ = modify;
    }

    /**
     * Sets the modify value.
     *
     * @param value the modify value.
     */
    protected void setModifyValue(String value) {
        modifyValue_ = value;
    }

    /**
     * Sets a new value for {@link java.util.Properties property}.
     *
     * @param newValue the new value
     */
    public void setNewValue(Object newValue) {
        newValue_ = newValue;
    }

    /**
     * Sets the {@link java.text.DecimalFormat DecimalFormat} or {@link java.time.format.DateTimeFormatter DateTimeFormatter}
     * pattern to be used with {@link EntryDate} or {@link EntryInt} respectively.
     *
     * @param pattern the pattern
     */
    protected void setPattern(String pattern) {
        pattern_ = pattern;
    }

    /**
     * Sets the {@link EntryDate.Units unit} value to apply to calculations.
     *
     * @param unit the {@link EntryDate.Units unit}
     */
    protected void setUnit(EntryDate.Units unit) {
        unit_ = unit;
    }
}
