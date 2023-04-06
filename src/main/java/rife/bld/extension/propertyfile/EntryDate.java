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

import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.function.IntFunction;

/**
 * Declares the modifications to be made to a {@link java.util.Properties Properties} file.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class EntryDate extends EntryBase {
    /**
     * Creates a new date {@link Entry entry}.
     *
     * @param key the required property key
     */
    public EntryDate(String key) {
        super(key);
    }

    /**
     * Set the new {@link java.util.Properties property} value to an {@link Instant}
     *
     * @param instant the {@link Instant} to set the value to.
     */
    public EntryDate set(Instant instant) {
        setNewValue(instant);
        return this;
    }

    /**
     * Set the new {@link java.util.Properties property} value to an {@link LocalDate}
     *
     * @param date the {@link LocalDate} to set the value to.
     */
    public EntryDate set(LocalDate date) {
        setNewValue(date);
        return this;
    }

    /**
     * Set the new {@link java.util.Properties property} value to an {@link LocalDateTime}
     *
     * @param date the {@link LocalDateTime} to set the value to.
     */
    public EntryDate set(LocalDateTime date) {
        setNewValue(date);
        return this;
    }

    /**
     * Set the new {@link java.util.Properties property} value to an {@link ZonedDateTime}
     *
     * @param date the {@link ZonedDateTime} to set the value to.
     */
    public EntryDate set(ZonedDateTime date) {
        setNewValue(date);
        return this;
    }

    /**
     * Set the new {@link java.util.Properties property} value to an {@link LocalTime}
     *
     * @param time the {@link LocalTime} to set the value to.
     */
    public EntryDate set(LocalTime time) {
        setNewValue(time);
        return this;
    }

    /**
     * Set the new {@link java.util.Properties property} value to an {@link Calendar}
     *
     * @param cal the {@link Calendar} to set the value to.
     */
    public EntryDate set(Calendar cal) {
        setNewValue(cal);
        return this;
    }

    /**
     * Set the new {@link java.util.Properties property} value to an {@link Date}
     *
     * @param date the {@link Date} to set the value to.
     */
    public EntryDate set(Date date) {
        setNewValue(date);
        return this;
    }

    /**
     * Sets the new value to now.
     */
    public EntryDate now() {
        setNewValue("now");
        return this;
    }

    /**
     * Creates a new {@link EntryDate entry}.
     *
     * @param calc the calculation function.
     */
    public EntryDate calc(IntFunction<Integer> calc) {
        setCalc(calc);
        return this;
    }

    /**
     * <p>Sets the pattern for {@link EntryInt} and {@link EntryDate} to
     * {@link java.text.DecimalFormat DecimalFormat} and {@link java.time.format.DateTimeFormatter DateTimeFormatter}
     * respectively.</p>
     *
     * @param pattern the pattern
     */
    public EntryDate pattern(String pattern) {
        setPattern(pattern);
        return this;
    }

    /**
     * Sets the {@link Units unit} value to apply to calculations for {@link EntryDate}.
     *
     * @param unit the {@link Units unit}
     */
    public EntryDate unit(Units unit) {
        setUnit(unit);
        return this;
    }

    /**
     * Sets the {@link EntryDate entry} up for deletion.
     */
    public EntryDate delete() {
        setDelete(true);
        return this;
    }

    /**
     * The units available for {@link EntryDate} calculations.
     *
     * <uL>
     * <li>{@link Units#SECOND SECOND}</li>
     * <li>{@link Units#MINUTE MINUTE}</li>
     * <li>{@link Units#HOUR HOUR}</li>
     * <li>{@link Units#DAY DAY}</li>
     * <li>{@link Units#WEEK WEEK}</li>
     * <li>{@link Units#MONTH MONTH}</li>
     * <li>{@link Units#YEAR YEAR}</li>
     * </uL>
     */
    public enum Units {
        SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
    }
}
