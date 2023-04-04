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
 * Declares the modifications to be made to a {@link java.util.Properties Properties} file.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @author <a href="https://github.com/gbevin">Geert Bevin</a>
 * @since 1.0
 */
public class Entry {
    private String key;
    private String defaultValue;
    private String newValue;
    private String modifyValue;
    private boolean isDelete;
    private Types type = Types.STRING;
    private String pattern = "";
    private Units unit = Units.DAY;
    private IntFunction<Integer> calc;
    private BiFunction<String, String, String> modify;

    /**
     * Creates a new {@link Entry entry}.
     *
     * @param key the required property key
     */
    public Entry(String key) {
        this.key = key;
    }

    /**
     * Creates a new {@link Entry entry}.
     *
     * @param key  the required property key
     * @param type the value {@link Types Type}
     */
    public Entry(String key, Types type) {
        this.key = key;
        this.type = type;
    }

    /**
     * Returns the value to be used in the {@link #modify} function.
     */
    public String getModifyValue() {
        return modifyValue;
    }

    /**
     * Returns the modify function.
     */
    public BiFunction<String, String, String> getModify() {
        return modify;
    }

    /**
     * Set the modify function.
     */
    public void setModify(BiFunction<String, String, String> modify) {
        this.modify = modify;
    }

    /**
     * Set the modify function.
     *
     * @param value the value to perform a modification with
     */
    public void setModify(String value, BiFunction<String, String, String> modify) {
        this.modifyValue = value;
        this.modify = modify;
    }

    /**
     * Creates a new {@link Entry entry}.
     *
     * @param value  the value to perform a modification with
     * @param modify the modification function
     */
    public Entry modify(String value, BiFunction<String, String, String> modify) {
        modifyValue = value;
        setModify(modify);
        return this;
    }

    /**
     * Creates a new {@link Entry entry}.
     *
     * @param modify the modification function
     */
    public Entry modify(BiFunction<String, String, String> modify) {
        setModify(modify);
        return this;
    }

    /**
     * Returns {@code true} if the {@link Entry} is to be deleted.
     */
    public boolean isDelete() {
        return isDelete;
    }

    /**
     * Sets whether the {@link Entry} should be deleted.
     */
    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    /**
     * Returns the calculation function.
     */
    public IntFunction<Integer> getCalc() {
        return calc;
    }

    /**
     * Sets the calculation function.
     */
    public void setCalc(IntFunction<Integer> calc) {
        this.calc = calc;
    }

    /**
     * Creates a new {@link Entry entry}.
     *
     * @param calc the calculation function.
     */
    public Entry calc(IntFunction<Integer> calc) {
        setCalc(calc);
        return this;
    }

    /**
     * Returns the key of the {@link java.util.Properties property}.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of the {@link java.util.Properties property}.
     *
     * @param key the {@link java.util.Properties property} key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the default value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * <p>Sets the initial value to set the {@link java.util.Properties property} to, if not already defined.</p>
     *
     * <p>The {@code now} keyword can be used for {@link Types#DATE Types.DATE}</p>
     *
     * @param defaultValue the default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Return the value {@link Types Type}.
     */
    public Types getType() {
        return type;
    }

    /**
     * Sets the value {@link Types Type}, if none is specified {@link Types#STRING Types.STRING} is assumed.
     *
     * @param type the value {@link Types Type}
     */
    public void setType(Types type) {
        this.type = type;
    }

    /**
     * Returns the pattern.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the {@link java.text.DecimalFormat DecimalFormat} or {@link java.text.SimpleDateFormat SimpleDateFormat}
     * pattern to be used with {@link Types#INT Types.INT} or {@link Types#DATE Types.DATE} respectively.
     *
     * @param pattern the pattern
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Return the {@link Units unit}.
     */
    public Units getUnit() {
        return unit;
    }

    /**
     * Sets the {@link Units unit} value to apply to {@link Calc#ADD add} and {@link Calc#SUB subtract} calculations
     * for {@link Types#DATE Types.DATE}.
     *
     * @param unit the {@link Units unit}
     */
    public void setUnit(Units unit) {
        this.unit = unit;
    }

    /**
     * Sets the key of the {@link java.util.Properties property}.
     *
     * @param key the {@link java.util.Properties property} key
     */
    @SuppressWarnings("unused")
    public Entry key(String key) {
        setKey(key);
        return this;
    }

    /**
     * Returns the new value to set the {@link java.util.Properties property)} to.
     */
    public String getNewValue() {
        return newValue;
    }

    /**
     * Sets a new value to set the {@link java.util.Properties property} to, regardless of its previous value.
     *
     * <p>The {@code now} keyword can be used for {@link Types#DATE Types.DATE}</p>
     *
     * @param newValue the {@link java.util.Properties property} new value
     */
    @SuppressWarnings("unused")
    public Entry set(Object newValue) {
        if (newValue != null) {
            this.newValue = String.valueOf(newValue);
        } else {
            this.newValue = null;
        }
        return this;
    }

    /**
     * <p>Sets the initial value to set the {@link java.util.Properties property} to, if not already defined.</p>
     *
     * <p>The {@code now} keyword can be used for {@link Types#DATE Types.DATE}</p>
     *
     * @param defaultValue the default value
     */
    @SuppressWarnings("unused")
    public Entry defaultValue(Object defaultValue) {
        if (defaultValue != null) {
            setDefaultValue(String.valueOf(defaultValue));
        } else {
            setDefaultValue(null);
        }
        return this;
    }

    /**
     * Sets the value {@link Types Type}, if none is specified {@link Types#STRING Types.STRING} is assumed.
     *
     * @param type the value {@link Types Type}
     */
    public Entry type(Types type) {
        setType(type);
        return this;
    }

    /**
     * <p>Sets the pattern for {@link Types#INT Types.INT} and {@link Types#DATE Types.DATE} to
     * {@link java.text.DecimalFormat DecimalFormat} and {@link java.text.SimpleDateFormat SimpleDateFormat}
     * respectively.</p>
     *
     * @param pattern the pattern
     */
    public Entry pattern(String pattern) {
        setPattern(pattern);
        return this;
    }

    /**
     * Sets the {@link Units unit} value to apply to {@link Calc#ADD add} and {@link Calc#SUB subtract} calculations
     * for {@link Types#DATE Types.DATE}.
     *
     * @param unit the {@link Units unit}
     */
    @SuppressWarnings("unused")
    public Entry unit(Units unit) {
        setUnit(unit);
        return this;
    }

    /**
     * Sets the {@link Entry entry} up for deletion.
     */
    public Entry delete() {
        isDelete = true;
        return this;
    }

    /**
     * The available datatypes.
     *
     * <uL>
     * <li>{@link Types#DATE DATE}</li>
     * <li>{@link Types#INT INT}</li>
     * <li>{@link Types#STRING STRING}</li>
     * </uL>
     */
    public enum Types {
        DATE, INT, STRING
    }

    /**
     * The units available for {@link Types#DATE Type.DATE} {@link Calc#ADD add}
     * and {@link Calc#SUB subtract} calculations.
     *
     * <uL>
     * <li>{@link Units#SECOND SECOND}</li>
     * <li>{@link Units#MINUTE MINUTE}</li>
     * <li>{@link Units#MILLISECOND MILLISECOND}</li>
     * <li>{@link Units#HOUR HOUR}</li>
     * <li>{@link Units#DAY DAY}</li>
     * <li>{@link Units#WEEK WEEK}</li>
     * <li>{@link Units#MONTH MONTH}</li>
     * <li>{@link Units#YEAR YEAR}</li>
     * </uL>
     */
    public enum Units {
        MILLISECOND, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
    }
}
