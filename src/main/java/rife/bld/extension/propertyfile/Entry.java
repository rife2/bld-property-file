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

/**
 * <p>Declares the edits to be made to a {@link java.util.Properties Properties} file.</p>
 *
 * <p>The rules used when setting a {@link java.util.Properties property} value are:</p>
 *
 * <ul>
 * <li>If only value is specified, the property is set to it regardless of its previous value.</li>
 * <li>If only default value is specified and the property previously existed, it is unchanged.</li>
 * <li>If only default value is specified and the property did not exist, the property is set to the default value.</li>
 * <li>If value and default value are both specified and the property previously existed, the property is set to value.</li>
 * <li>If value and default value are both specified and the property did not exist, the property is set to the default value.</li>
 * </ul>
 *
 * <p>{@link Operations Operations} occur after the rules are evaluated.</p>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class Entry {
    private String key;
    private String value;
    private String defaultValue;
    private Types type = Types.STRING;
    private Operations operation = Operations.SET;
    private String pattern = "";
    private Units unit = Units.DAY;

    /**
     * Creates a new {@link Entry entry} Entry.
     *
     * @param key the required property key
     */
    public Entry(String key) {
        this.key = key;
    }

    /**
     * Returns the name of the {@link java.util.Properties property} name/value pair.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the name of the {@link java.util.Properties property} name/value pair.
     *
     * @param key the {@link java.util.Properties property} key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the value of the {@link java.util.Properties property}.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the {@link java.util.Properties property}.
     *
     * @param value the {@link java.util.Properties property} value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the default value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * <p>Sets the initial value to set for the {@link java.util.Properties property} if not already defined.</p>
     *
     * <p>The {@code now} keyword can be used for {@link Types#DATE Types.DATE}</p>
     *
     * @param defaultValue the default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Return the value {@link Types Type}/
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
     * Return the {@link Operations Operation}.
     */
    public Operations getOperation() {
        return operation;
    }

    /**
     * Sets the {@link Operations Operation} to be performed on the {@link java.util.Properties property} value,
     *
     * @param operation the entry {@link Operations Operation}
     */
    public void setOperation(Operations operation) {
        this.operation = operation;
    }

    /**
     * Returns the pattern.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * <p>Parses the value of {@link Types#INT Types.INT} and {@link Types#DATE Types.DATE} to
     * {@link java.text.DecimalFormat DecimalFormat} and {@link java.text.SimpleDateFormat SimpleDateFormat}
     * respectively.</p>
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
     * Sets the {@link Units unit} value to apply to {@link Operations#ADD Operations.ADD}
     * and {@link Operations#SUBTRACT Operations.SUBTRACT} for {@link Types#DATE Types.DATE}.
     *
     * @param unit the {@link Units unit}
     */
    public void setUnit(Units unit) {
        this.unit = unit;
    }

    /**
     * Sets the name of the {@link java.util.Properties property} name/value pair.
     *
     * @param key the {@link java.util.Properties property} key
     */

    @SuppressWarnings("unused")
    public Entry key(String key) {
        setKey(key);
        return this;
    }

    /**
     * Sets the value of the {@link java.util.Properties property}.
     *
     * @param value the {@link java.util.Properties property} value
     */
    @SuppressWarnings("unused")
    public Entry value(Object value) {
        if (value != null) {
            setValue(String.valueOf(value));
        } else {
            setValue(null);
        }
        return this;
    }

    /**
     * <p>Sets the initial value to set for the {@link java.util.Properties property} if not already defined.</p>
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
     * Sets the {@link Operations Operation} to be performed on the {@link java.util.Properties property} value,
     *
     * @param operation the entry {@link Operations Operation}
     */
    @SuppressWarnings("unused")
    public Entry operation(Operations operation) {
        setOperation(operation);
        return this;
    }

    /**
     * <p>Parses the value of {@link Types#INT Types.INT} and {@link Types#DATE Types.DATE} to
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
     * Sets the {@link Units unit} value to apply to {@link Operations#ADD Operations.ADD}
     * and {@link Operations#SUBTRACT Operations.SUBTRACT} for {@link Types#DATE Types.DATE}.
     *
     * @param unit the {@link Units unit}
     */
    @SuppressWarnings("unused")
    public Entry unit(Units unit) {
        setUnit(unit);
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
     * The operations available for all {@link Types Types}.
     *
     * <uL>
     * <li>{@link Operations#ADD ADD} adds a value to an {@link Entry entry}</li>
     * <li>{@link Operations#DELETE DELETE} deletes an entry</li>
     * <li>{@link Operations#SET SET} sets the entry value. This is the default operation</li>
     * <li>{@link Operations#SUBTRACT SUBTRACT} subtracts a value from the {@link Entry entry}.
     * For {@link Types#INT Types.INT} and {@link Types#DATE Types.DATE} only.</li>
     * </uL>
     */
    public enum Operations {
        ADD, DELETE, SET, SUBTRACT
    }

    /**
     * The units available for {@link Types#DATE Type.DATE} with {@link Operations#ADD Operations>ADD}
     * and {@link Operations#SUBTRACT Operations.SUBTRACT}.
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
