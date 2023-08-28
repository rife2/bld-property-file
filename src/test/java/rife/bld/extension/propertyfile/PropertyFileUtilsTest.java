/*
 * Copyright 2023 the original author or authors.
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
import rife.tools.Localization;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat; // NOPMD
import static org.assertj.core.api.Assertions.assertThatCode; // NOPMD
import static rife.bld.extension.propertyfile.Calc.ADD; // NOPMD
import static rife.bld.extension.propertyfile.Calc.SUB; // NOPMD

/**
 * PropertyFileUtilsTest class
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
class PropertyFileUtilsTest {
    final static int dayOfYear = LocalDate.now().getDayOfYear();
    final static Properties p = new Properties();
    final static String t = "test";

    public Entry newEntry() {
        p.clear();
        return new Entry("version.major").set("1");
    }

    public EntryDate newEntryDate() {
        p.clear();
        return new EntryDate("adate").pattern("D");
    }

    public EntryInt newEntryInt() {
        p.clear();
        return new EntryInt("version.patch");
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void parseDateSub() throws Exception {
        var entryDate = newEntryDate();
        entryDate.setCalc(SUB);
        PropertyFileUtils.processDate(t, p, entryDate.now(), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(now-3)").isEqualTo(String.valueOf(dayOfYear - 1));

        entryDate.setCalc(v -> v - 2);
        PropertyFileUtils.processDate(t, p, entryDate.now(), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(now-2)").isEqualTo(String.valueOf(dayOfYear - 2));

        entryDate.setCalc(SUB);
        PropertyFileUtils.processDate(t, p, entryDate.set(new Date()), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(date-1)").isEqualTo(String.valueOf(dayOfYear - 1));

        entryDate.setCalc(v -> v - 2);
        PropertyFileUtils.processDate(t, p, entryDate.set(Calendar.getInstance()), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(cal-2)").isEqualTo(String.valueOf(dayOfYear - 2));

        entryDate.setCalc(v -> v - 3);
        PropertyFileUtils.processDate(t, p, entryDate.set(LocalDate.now()),
                true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(LocalDate-3)").isEqualTo(String.valueOf(dayOfYear - 3));
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void parseIntSubTest() throws Exception {
        var entryInt = newEntryInt();
        entryInt.calc(SUB);
        entryInt.setPattern("0000");
        PropertyFileUtils.processInt(t, p, entryInt.defaultValue("0017"), true);
        assertThat(p.getProperty(entryInt.getKey())).as("sub(0017)").isEqualTo("0016");

        PropertyFileUtils.processInt(t, p, entryInt.set(16).calc(v -> v - 3), true);
        assertThat(p.getProperty(entryInt.getKey())).as("sub(16)-3").isEqualTo("0013");
    }

    @Test
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    void parseStringAppend() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry.set(1));
        PropertyFileUtils.processString(p, entry.modify("-foo", String::concat));
        assertThat(p.getProperty(entry.getKey())).as(String.format("processString(%s, %s)", entry.getKey(),
                entry.getNewValue())).isEqualTo("1-foo");
    }

    @Test
    void parseStringCap() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry.set(t).modify("", (v, s) -> v.toUpperCase(Localization.getLocale())));
        assertThat(p.getProperty(entry.getKey())).as("capitalize").isEqualTo(t.toUpperCase(Localization.getLocale()));

    }

    @Test
    void parseStringCat() {
        var entry = newEntry();
        entry.set(t).setModify("-foo", String::concat);
        PropertyFileUtils.processString(p, entry);
        assertThat(p.getProperty(entry.getKey())).as("replace").isEqualTo(t + "-foo");
    }

    @Test
    void parseStringPrepend() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry.set(1));
        PropertyFileUtils.processString(p, entry.modify("foo-", (v, s) -> s + v));
        assertThat(p.getProperty(entry.getKey())).as(String.format("processString(%s, %s)", entry.getKey(), entry.getNewValue()))
                .isEqualTo("foo-1");
    }

    @Test
    void parseStringReplace() {
        var entry = newEntry();
        entry.set(t).setModify("T", (v, s) -> v.replace("t", s));
        PropertyFileUtils.processString(p, entry);
        assertThat(p.getProperty(entry.getKey())).as("replace(t -> T)").isEqualTo("TesT");

    }

    @Test
    void parseStringSub() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry.set(t).modify((v, s) -> v.substring(1)));
        assertThat(p.getProperty(entry.getKey())).as("substring(1)").isEqualTo(t.substring(1));
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void parseTimeTest() throws Exception {
        var entry = new EntryDate("time").pattern("m");
        var time = LocalTime.now();

        entry.setCalc(ADD);
        PropertyFileUtils.processDate(t, p, entry.set(time).unit(EntryDate.Units.MINUTE), true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(now+1)")
                .isEqualTo(String.valueOf(time.plusMinutes(1).getMinute()));

        entry.setCalc(SUB);
        PropertyFileUtils.processDate(t, p, entry.set(time).unit(EntryDate.Units.HOUR).pattern("H"), true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(now+1)")
                .isEqualTo(String.valueOf(time.minusHours(1).getHour()));
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void processDateAddTest() throws Exception {
        var entryDate = newEntryDate();
        entryDate.setCalc(ADD);
        PropertyFileUtils.processDate(t, p, entryDate.now(), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(now+1)").isEqualTo(String.valueOf(dayOfYear + 1));

        PropertyFileUtils.processDate(t, p, entryDate.now().calc(v -> v + 3), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(now+3)").isEqualTo(String.valueOf(dayOfYear + 3));

        entryDate.setCalc(ADD);
        PropertyFileUtils.processDate(t, p, entryDate.set(ZonedDateTime.now()), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(ZonedDateTime+1)")
                .isEqualTo(String.valueOf(dayOfYear + 1));

        PropertyFileUtils.processDate(t, p, entryDate.set(Instant.now()).calc(v -> v + 2), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(Instant+2)").isEqualTo(String.valueOf(dayOfYear + 2));

        entryDate.setCalc(v -> v + 3);
        PropertyFileUtils.processDate(t, p, entryDate.set(LocalDateTime.now()), true);
        assertThat(p.getProperty(entryDate.getKey())).as("processDate(LocalDteTime+2)").isEqualTo(String.valueOf(dayOfYear + 3));
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void processIntAddTest() throws Exception {
        var entryInt = newEntryInt();
        entryInt.calc(ADD);
        entryInt.setDefaultValue("-1");
        PropertyFileUtils.processInt(t, p, entryInt, true);
        assertThat(p.getProperty(entryInt.getKey())).as("add(-1)").isEqualTo("0");

        entryInt.setKey("anint");
        entryInt.setDefaultValue("0");
        PropertyFileUtils.processInt(t, p, entryInt, true);
        assertThat(p.getProperty(entryInt.getKey())).as("add(0)").isEqualTo("1");
        PropertyFileUtils.processInt(t, p, entryInt, true);
        assertThat(p.getProperty(entryInt.getKey())).as("add(1)").isEqualTo("2");

        entryInt.setKey("formatted.int");
        entryInt.setDefaultValue("0013");
        entryInt.setPattern("0000");
        PropertyFileUtils.processInt(t, p, entryInt, true);
        assertThat(p.getProperty(entryInt.getKey())).as("add(0013)").isEqualTo("0014");
        PropertyFileUtils.processInt(t, p, entryInt, true);
        assertThat(p.getProperty(entryInt.getKey())).as("add(0014)").isEqualTo("0015");

        entryInt.calc(v -> v + 2);
        PropertyFileUtils.processInt(t, p, entryInt, true);
        assertThat(p.getProperty(entryInt.getKey())).as("add(0015)+2").isEqualTo("0017");
    }

    @Test
    void processStringTest() {
        var entry = newEntry();
        PropertyFileUtils.processString(p, entry);

        assertThat(entry.getNewValue()).as(String.format("processString(%s, %s)", entry.getKey(), entry.getNewValue()))
                .isEqualTo(p.getProperty(entry.getKey()));

        entry.setKey("version.minor");

        PropertyFileUtils.processString(p, entry.set(0));
        assertThat(entry.getNewValue().toString()).as(String.format("processString(%s, %s)", entry.getKey(), entry.getNewValue()))
                .isEqualTo(p.getProperty(entry.getKey()));
    }

    @Test
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    void savePropertiesTest() throws Exception {
        var p = new Properties();
        var test = "test";

        p.put(test, test);

        var tmp = File.createTempFile(test, ".properties");

        assertThatCode(() -> PropertyFileUtils.saveProperties(tmp, "Generated file - do not modify!", p))
                .as("save properties").doesNotThrowAnyException();

        assertThat(PropertyFileUtils.loadProperties(t, tmp, p)).as("load properties").isTrue();

        assertThat(p.getProperty(test)).as("read property").isEqualTo(test);

        tmp.deleteOnExit();
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
        assertThatCode(() -> PropertyFileUtils.warn("command", "message", new IOException(t), true))
                .hasMessage(t).isInstanceOf(IOException.class);
        assertThatCode(() -> PropertyFileUtils.warn("command", t, new Exception(t), false))
                .as("failOnWarning = false").doesNotThrowAnyException();
    }
}