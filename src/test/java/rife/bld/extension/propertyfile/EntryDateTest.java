/*
 * Copyright 2023-2026 the original author or authors.
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

import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class EntryDateTest {

    @Test
    void shouldSetNow() {
        var entry = new EntryDate("key").now();
        assertThat(entry.newValue()).isEqualTo("now");
    }

    @Test
    void shouldSetPattern() {
        var entry = new EntryDate("key").pattern("yyyy-MM-dd");
        assertThat(entry.pattern()).isEqualTo("yyyy-MM-dd");
    }

    @Test
    void shouldSetInstant() {
        var instant = Instant.now();
        var entry = new EntryDate("key").set(instant);
        assertThat(entry.newValue()).isEqualTo(instant);
    }

    @Test
    void shouldSetLocalDate() {
        var date = LocalDate.now();
        var entry = new EntryDate("key").set(date);
        assertThat(entry.newValue()).isEqualTo(date);
    }

    @Test
    void shouldSetLocalDateTime() {
        var date = LocalDateTime.now();
        var entry = new EntryDate("key").set(date);
        assertThat(entry.newValue()).isEqualTo(date);
    }

    @Test
    void shouldSetZonedDateTime() {
        var date = ZonedDateTime.now();
        var entry = new EntryDate("key").set(date);
        assertThat(entry.newValue()).isEqualTo(date);
    }

    @Test
    void shouldSetLocalTime() {
        var time = LocalTime.now();
        var entry = new EntryDate("key").set(time);
        assertThat(entry.newValue()).isEqualTo(time);
    }

    @Test
    void shouldSetCalendar() {
        var cal = Calendar.getInstance();
        var entry = new EntryDate("key").set(cal);
        assertThat(entry.newValue()).isEqualTo(cal);
    }

    @Test
    @SuppressWarnings("PMD.ReplaceJavaUtilDate")
    void shouldSetDate() {
        var date = new Date();
        var entry = new EntryDate("key").set(date);
        assertThat(entry.newValue()).isEqualTo(date);
    }

    @Test
    void shouldSetAndGetUnit() {
        var entry = new EntryDate("key");
        assertThat(entry.unit()).isEqualTo(EntryDate.Units.DAY);
        entry.unit(EntryDate.Units.YEAR);
        assertThat(entry.unit()).isEqualTo(EntryDate.Units.YEAR);
    }
}
