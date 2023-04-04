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

import org.junit.jupiter.api.Test;
import rife.tools.Localization;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
class PropertyFileUtilsTest {
    final Properties p = new Properties();
    final String t = "test";

    @Test
    void processStringTest() {
        var entry = new Entry("version.major").set("1");

        PropertyFileUtils.processString(p, entry);

        assertThat(entry.getNewValue()).as(String.format("processString(%s, %s)", entry.getKey(), entry.getNewValue()))
                .isEqualTo(p.getProperty(entry.getKey()));

        entry.setKey("version.minor");

        PropertyFileUtils.processString(p, entry.set(0));
        assertThat(entry.getNewValue()).as(String.format("processString(%s, %s)", entry.getKey(), entry.getNewValue()))
                .isEqualTo(p.getProperty(entry.getKey()));

        // APPEND
        PropertyFileUtils.processString(p, entry.modify("-foo", String::concat));
        assertThat(p.getProperty(entry.getKey())).as(String.format("processString(%s, %s)", entry.getKey(), entry.getNewValue()))
                .isEqualTo("0-foo");

        // PREPEND
        PropertyFileUtils.processString(p, entry.modify("foo-", (v, s) -> s + v));
        assertThat(p.getProperty(entry.getKey())).as(String.format("processString(%s, %s)", entry.getKey(), entry.getNewValue()))
                .isEqualTo("foo-0");
        // CAP
        PropertyFileUtils.processString(p, entry.set(t).modify((v, s) -> v.toUpperCase(Localization.getLocale())));
        assertThat(p.getProperty(entry.getKey())).as("capitalize").isEqualTo(t.toUpperCase(Localization.getLocale()));

        // REPLACE
        entry.set(t).setModify("T", (v, s) -> v.replace("t", s));
        PropertyFileUtils.processString(p, entry);
        assertThat(p.getProperty(entry.getKey())).as("replace").isEqualTo("TesT");

        // SUBSTRING
        entry.set(t).setModify((v, s) -> v.substring(1));
        PropertyFileUtils.processString(p, entry);
        assertThat(p.getProperty(entry.getKey())).as("substring").isEqualTo(t.substring(1));
    }

    @Test
    void processIntTest() {
        var entry = new Entry("version.patch").type(Entry.Types.INT);

        // ADD
        entry.calc(ADD);
        entry.setDefaultValue("-1");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("add(-1)").isEqualTo("0");

        entry.setKey("anint");
        entry.setDefaultValue("0");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("add(0)").isEqualTo("1");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("add(1)").isEqualTo("2");

        entry.setKey("formated.int");
        entry.setDefaultValue("0013");
        entry.setPattern("0000");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("add(0013)").isEqualTo("0014");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("add(0014)").isEqualTo("0015");

        entry.setKey("formated.int");
        entry.calc(v -> v + 2);
        entry.setDefaultValue("0013");
        entry.setPattern("0000");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("add(0013)+2").isEqualTo("0017");

        // SUBTRACT
        entry.calc(SUB);
        entry.setPattern("0000");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("sub(0017)").isEqualTo("0016");

        PropertyFileUtils.processInt(t, p, entry.calc(v -> v - 3), true);
        assertThat(p.getProperty(entry.getKey())).as("sub(0017)-3").isEqualTo("0013");
    }

    @Test
    void processDateTest() {
        var entry = new Entry("adate", Entry.Types.DATE).pattern("D");
        var day = new SimpleDateFormat(entry.getPattern(), Localization.getLocale()).format(new Date());
        var dayInt = Integer.parseInt(day);

        assertThat(PropertyFileUtils.processDate(t, p, entry.set("a"), false)).as("processDate(a)").isFalse();

        PropertyFileUtils.processDate(t, p, entry.set("99"), true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(99)").isEqualTo("99");

        PropertyFileUtils.processDate(t, p, entry.set("noew"), true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(now)").isEqualTo(day);

        // ADD
        entry.setCalc(ADD);
        PropertyFileUtils.processDate(t, p, entry.set(dayInt), true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(now+1)").isEqualTo(String.valueOf(dayInt + 1));

        entry.setCalc(v -> v + 3);
        PropertyFileUtils.processDate(t, p, entry.set(dayInt), true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(now+3)").isEqualTo(String.valueOf(dayInt + 3));

        // SUBTRACT
        entry.setCalc(SUB);
        PropertyFileUtils.processDate(t, p, entry.set(dayInt), true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(now-3)").isEqualTo(String.valueOf(dayInt - 1));

        entry.setCalc(v -> v - 2);
        PropertyFileUtils.processDate(t, p, entry.set(dayInt), true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(now-2)").isEqualTo(String.valueOf(dayInt - 2));
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
    void savePropertiesTest() throws IOException {
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
}