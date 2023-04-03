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
import static rife.bld.extension.propertyfile.Calc.*;

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
    void currentValueTest() {
        String prev = "previous";
        String value = "value";
        String defaultValue = "defaultValue";
        var operation = Entry.Operations.SET;

        // If only value is specified, the property is set to it regardless of its previous value.
        assertThat(PropertyFileUtils.currentValue(prev, value, null, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo(value);

        // If only defaultValue is specified and the property previously existed, it is unchanged.
        assertThat(PropertyFileUtils.currentValue(prev, null, defaultValue, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo(prev);

        // If only defaultValue is specified and the property did not exist, the property is set to defaultValue.
        assertThat(PropertyFileUtils.currentValue(null, null, defaultValue, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo(defaultValue);

        // If value and defaultValue are both specified and the property previously existed, the property is set to value.
        assertThat(PropertyFileUtils.currentValue(prev, value, defaultValue, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo(value);

        // If value and defaultValue are both specified and the property did not exist, the property is set to defaultValue.
        assertThat(PropertyFileUtils.currentValue(null, value, defaultValue, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo(defaultValue);

        // ADD
        operation = Entry.Operations.ADD;

        assertThat(PropertyFileUtils.currentValue(null, value, defaultValue, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo(defaultValue);

        assertThat(PropertyFileUtils.currentValue(prev, value, null, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo(prev);

        assertThat(PropertyFileUtils.currentValue(null, value, null, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo("");

        assertThat(PropertyFileUtils.currentValue(null, value, defaultValue, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo(defaultValue);

        assertThat(PropertyFileUtils.currentValue(null, null, null, operation))
                .as(String.format("currentValue(%s,%s,%s,%s)", prev, value, defaultValue, operation)).isEqualTo("");
    }

    @Test
    void processStringTest() {
        var entry = new Entry("version.major").value("1");

        PropertyFileUtils.processString(p, entry);

        assertThat(entry.getValue()).as("processString(entry.getKey(), entry.getValue())").isEqualTo(p.getProperty(entry.getKey()));

        entry.setKey("version.minor");
        entry.setValue("0");

        PropertyFileUtils.processString(p, entry);
        assertThat(entry.getValue()).as("processString(entry.getKey(), entry.getValue())").isEqualTo(p.getProperty(entry.getKey()));
    }

    @Test
    void processIntTest() {
        var entry = new Entry("version.patch").value("a").type(Entry.Types.INT);
        assertThat(PropertyFileUtils.processInt(t, p, entry, false)).as("parseInt(entry.getKey(), a)");

        // ADD
        entry.setOperation(Entry.Operations.ADD);

        entry.setValue("1");
        entry.setDefaultValue("-1");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 0)").isEqualTo("0");

        entry.setKey("anint");
        entry.setValue(null);
        entry.setDefaultValue("0");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 1)").isEqualTo("1");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 2)").isEqualTo("2");

        entry.setKey("formated.int");
        entry.setValue(null);
        entry.setDefaultValue("0013");
        entry.setPattern("0000");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 0014)").isEqualTo("0014");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 0015)").isEqualTo("0015");

        entry.setKey("formated.int");
        entry.setValue("2");
        entry.setDefaultValue("0013");
        entry.setPattern("0000");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 0017)").isEqualTo("0017");

        // SUBTRACT
        entry.setOperation(Entry.Operations.SUBTRACT);
        entry.setValue(null);
        entry.setDefaultValue("0013");
        entry.setPattern("0000");
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 0016)").isEqualTo("0016");

        // CALC
        entry.setOperation(Entry.Operations.SET);
        entry.setValue(null);
        entry.setDefaultValue("0013");
        entry.setPattern("0000");
        entry.setCalc(v -> v + 23);
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 0039)").isEqualTo("0039");

        // CALC OP
        entry.setOperation(Entry.Operations.SET);
        entry.setValue(null);
        entry.setDefaultValue("0013");
        entry.setPattern("0000");
        entry.setCalc(Calc::add);
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 0040)").isEqualTo("0040");

        // CALC OP
        entry.setOperation(Entry.Operations.SET);
        entry.setValue(null);
        entry.setDefaultValue("0013");
        entry.setPattern("0000");
        entry.setCalc(SUB);
        PropertyFileUtils.processInt(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processInt(entry.getKey(), 0039)").isEqualTo("0039");
    }

    @Test
    void processDateTest() {
        var entry = new Entry("adate").type(Entry.Types.DATE).pattern("D").value("1");
        var day = new SimpleDateFormat(entry.getPattern(), Localization.getLocale()).format(new Date());
        var dayInt = Integer.parseInt(day);

        entry.setValue("a");
        assertThat(PropertyFileUtils.processDate(t, p, entry, false)).as("processDate(entry.getKey(), a)").isFalse();

        entry.setValue("99");
        PropertyFileUtils.processDate(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(entry.getKey(), 99)").isEqualTo("99");

        entry.setValue("now");
        PropertyFileUtils.processDate(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(entry.getKey(), now)").isEqualTo(day);

        // ADD
        entry.setOperation(Entry.Operations.ADD);

        entry.setValue("1");
        PropertyFileUtils.processDate(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(entry.getKey(), now+1)").isEqualTo(String.valueOf(dayInt + 1));

        entry.setValue("2");
        PropertyFileUtils.processDate(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(entry.getKey(), now+3)").isEqualTo(String.valueOf(dayInt + 3));

        // SUBTRACT
        entry.setOperation(Entry.Operations.SUBTRACT);
        entry.setValue("3");
        PropertyFileUtils.processDate(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(entry.getKey(), now-3)").isEqualTo(String.valueOf(dayInt));

        entry.setOperation(Entry.Operations.SUBTRACT);
        entry.setValue("2");
        PropertyFileUtils.processDate(t, p, entry, true);
        assertThat(p.getProperty(entry.getKey())).as("processDate(entry.getKey(), now-2)").isEqualTo(String.valueOf(dayInt - 2));
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