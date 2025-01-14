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

import org.junit.jupiter.api.Test;
import rife.bld.operations.exceptions.ExitStatusException;
import rife.tools.Localization;

import java.io.File;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static rife.bld.extension.propertyfile.Calc.ADD;
import static rife.bld.extension.propertyfile.Calc.SUB;

/**
 * PropertyFileUtilsTest class
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PropertyFileUtilsTest {
    final static Properties p = new Properties();
    final static String t = "test";

    public Entry newEntry() {
        p.clear();
        return new Entry("version.major").set("1");
    }

    public EntryDate newEntryDate() {
        p.clear();
        return new EntryDate("aDate").pattern("D");
    }

    public EntryInt newEntryInt() {
        p.clear();
        return new EntryInt("version.patch");
    }

    @Test
    void parseDateSub() {
        var entryDate = newEntryDate().calc(SUB);
        PropertyFileUtils.processDate(p, entryDate.now());
        assertThat(p.getProperty(entryDate.key())).as("processDate(now-3)").isEqualTo(String.valueOf(
                LocalDateTime.now().minusDays(1).getDayOfYear()));

        entryDate.calc(v -> v - 2);
        PropertyFileUtils.processDate(p, entryDate.now());
        assertThat(p.getProperty(entryDate.key())).as("processDate(now-2)").isEqualTo(String.valueOf(
                LocalDateTime.now().minusDays(2).getDayOfYear()));

        entryDate.calc(SUB);
        PropertyFileUtils.processDate(p, entryDate.set(new Date()));
        assertThat(p.getProperty(entryDate.key())).as("processDate(date-1)").isEqualTo(String.valueOf(
                LocalDateTime.now().minusDays(1).getDayOfYear()));

        entryDate.calc(v -> v - 2);
        PropertyFileUtils.processDate(p, entryDate.set(Calendar.getInstance()));
        assertThat(p.getProperty(entryDate.key())).as("processDate(cal-2)").isEqualTo(String.valueOf(
                LocalDateTime.now().minusDays(2).getDayOfYear()));

        entryDate.calc(v -> v - 3);
        PropertyFileUtils.processDate(p, entryDate.set(LocalDate.now()));
        assertThat(p.getProperty(entryDate.key())).as("processDate(LocalDate-3)").isEqualTo(String.valueOf(
                LocalDateTime.now().minusDays(3).getDayOfYear()));
    }

    @Test
    void parseIntSubTest() {
        var entryInt = newEntryInt().calc(SUB).pattern("0000");
        PropertyFileUtils.processInt(p, entryInt.defaultValue("0017"));
        assertThat(p.getProperty(entryInt.key())).as("sub(0017)").isEqualTo("0016");

        PropertyFileUtils.processInt(p, entryInt.set(16).calc(v -> v - 3));
        assertThat(p.getProperty(entryInt.key())).as("sub(16)-3").isEqualTo("0013");
    }

    @Test
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    void parseStringAppend() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry.set(1));
        PropertyFileUtils.processString(p, entry.modify("-foo", String::concat));
        assertThat(p.getProperty(entry.key())).as(String.format("processString(%s, %s)", entry.key(),
                entry.newValue())).isEqualTo("1-foo");
    }

    @Test
    void parseStringCap() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry.set(t).modify("", (v, s) -> v.toUpperCase(Localization.getLocale())));
        assertThat(p.getProperty(entry.key())).as("capitalize").isEqualTo(t.toUpperCase(Localization.getLocale()));

    }

    @Test
    void parseStringCat() {
        var entry = newEntry();
        entry.set(t).modify("-foo", String::concat);
        PropertyFileUtils.processString(p, entry);
        assertThat(p.getProperty(entry.key())).as("replace").isEqualTo(t + "-foo");
    }

    @Test
    void parseStringFormat() {
        var entry = new Entry("foo").set("%.2f").pattern(3.14159f);
        PropertyFileUtils.processString(p, entry);
        assertThat(p.getProperty(entry.key())).as("format").isEqualTo("3.14");
    }


    @Test
    void parseStringPrepend() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry.set(1));
        PropertyFileUtils.processString(p, entry.modify("foo-", (v, s) -> s + v));
        assertThat(p.getProperty(entry.key())).as(String.format("processString(%s, %s)", entry.key(), entry.newValue()))
                .isEqualTo("foo-1");
    }

    @Test
    void parseStringReplace() {
        var entry = newEntry();
        entry.set(t).modify("T", (v, s) -> v.replace("t", s));
        PropertyFileUtils.processString(p, entry);
        assertThat(p.getProperty(entry.key())).as("replace(t -> T)").isEqualTo("TesT");
    }

    @Test
    void parseStringSub() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry.set(t).modify((v, s) -> v.substring(1)));
        assertThat(p.getProperty(entry.key())).as("substring(1)").isEqualTo(t.substring(1));
    }

    @Test
    void parseTimeTest() {
        var entry = new EntryDate("time").pattern("m");
        var time = LocalTime.now();

        entry.calc(ADD);
        PropertyFileUtils.processDate(p, entry.set(time).unit(EntryDate.Units.MINUTE));
        assertThat(p.getProperty(entry.key())).as("processDate(now+1)")
                .isEqualTo(String.valueOf(time.plusMinutes(1).getMinute()));

        entry.calc(SUB);
        PropertyFileUtils.processDate(p, entry.set(time).unit(EntryDate.Units.HOUR).pattern("H"));
        assertThat(p.getProperty(entry.key())).as("processDate(now+1)")
                .isEqualTo(String.valueOf(time.minusHours(1).getHour()));
    }

    @Test
    void processDateAddTest() {
        var entryDate = newEntryDate();
        entryDate.calc(ADD);
        PropertyFileUtils.processDate(p, entryDate.now());
        assertThat(p.getProperty(entryDate.key())).as("processDate(now+1)").isEqualTo(String.valueOf(
                LocalDateTime.now().plusDays(1).getDayOfYear()));

        PropertyFileUtils.processDate(p, entryDate.now().calc(v -> v + 3));
        assertThat(p.getProperty(entryDate.key())).as("processDate(now+3)").isEqualTo(String.valueOf(
                LocalDateTime.now().plusDays(3).getDayOfYear()));

        entryDate.calc(ADD);
        PropertyFileUtils.processDate(p, entryDate.set(ZonedDateTime.now()));
        assertThat(p.getProperty(entryDate.key())).as("processDate(ZonedDateTime+1)")
                .isEqualTo(String.valueOf(LocalDateTime.now().plusDays(1).getDayOfYear()));

        PropertyFileUtils.processDate(p, entryDate.set(Instant.now()).calc(v -> v + 2));
        assertThat(p.getProperty(entryDate.key())).as("processDate(Instant+2)").isEqualTo(String.valueOf(
                LocalDateTime.now().plusDays(2).getDayOfYear()));

        entryDate.calc(v -> v + 3);
        PropertyFileUtils.processDate(p, entryDate.set(LocalDateTime.now()));
        assertThat(p.getProperty(entryDate.key())).as("processDate(LocalDteTime+2)")
                .isEqualTo(String.valueOf(LocalDateTime.now().plusDays(3).getDayOfYear()));
    }

    @Test
    void processIntAddTest() {
        var entryInt = newEntryInt().calc(ADD).defaultValue("-1");
        PropertyFileUtils.processInt(p, entryInt);
        assertThat(p.getProperty(entryInt.key())).as("add(-1)").isEqualTo("0");

        entryInt.key("anInt").defaultValue("0");
        PropertyFileUtils.processInt(p, entryInt);
        assertThat(p.getProperty(entryInt.key())).as("add(0)").isEqualTo("1");
        PropertyFileUtils.processInt(p, entryInt);
        assertThat(p.getProperty(entryInt.key())).as("add(1)").isEqualTo("2");

        entryInt.key("formatted.int").defaultValue("0013").pattern("0000");
        PropertyFileUtils.processInt(p, entryInt);
        assertThat(p.getProperty(entryInt.key())).as("add(0013)").isEqualTo("0014");
        PropertyFileUtils.processInt(p, entryInt);
        assertThat(p.getProperty(entryInt.key())).as("add(0014)").isEqualTo("0015");

        entryInt.calc(v -> v + 2);
        PropertyFileUtils.processInt(p, entryInt);
        assertThat(p.getProperty(entryInt.key())).as("add(0015)+2").isEqualTo("0017");
    }

    @Test
    void processStringTest() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry);

        assertThat(entry.newValue()).as(String.format("processString(%s, %s)", entry.key(), entry.newValue()))
                .isEqualTo(p.getProperty(entry.key()));

        entry.key("version.minor");

        PropertyFileUtils.processString(p, entry.set(0));
        assertThat(entry.newValue().toString()).as(String.format("processString(%s, %s)", entry.key(), entry.newValue()))
                .isEqualTo(p.getProperty(entry.key()));
    }

    @Test
    void savePropertiesTest() throws Exception {
        var p = new Properties();
        var test = "test";

        p.put(test, test);

        var tmp = File.createTempFile(test, ".properties");

        assertThatCode(() -> PropertyFileUtils.saveProperties(tmp, "Generated file - do not modify!", p))
                .as("save properties").doesNotThrowAnyException();

        assertThat(PropertyFileUtils.loadProperties(t, tmp, p, false)).as("load properties").isTrue();

        assertThat(p.getProperty(test)).as("read property").isEqualTo(test);

        tmp.deleteOnExit();
    }

    @Test
    void testChangeKey() {
        var entry = new Entry("foo").key("bar");
        assertThat(entry.key()).isEqualTo("bar");

        entry.key("foo");
        assertThat(entry.key()).isEqualTo("foo");
    }

    @Test
    void testCurrentValue() {
        var value = "value";
        var defaultValue = "default";
        var newValue = "new";

        assertThat(PropertyFileUtils.currentValue(value, defaultValue, newValue)).as("all").isEqualTo(newValue);
        assertThat(PropertyFileUtils.currentValue(value, null, null)).as("value").isEqualTo(value);
        assertThat(PropertyFileUtils.currentValue(value, defaultValue, null)).as("value not default").isEqualTo(value);
        assertThat(PropertyFileUtils.currentValue(null, defaultValue, null)).as("default").isEqualTo(defaultValue);
        assertThat(PropertyFileUtils.currentValue(null, null, newValue)).as("new").isEqualTo(newValue);
    }

    @Test
    void testWarn() {
        assertThatCode(() -> PropertyFileUtils.warn("command", "message", true, false))
                .isInstanceOf(ExitStatusException.class);
        assertThatCode(() -> PropertyFileUtils.warn("command", t, false, false))
                .as("failOnWarning = false").doesNotThrowAnyException();
    }
}
