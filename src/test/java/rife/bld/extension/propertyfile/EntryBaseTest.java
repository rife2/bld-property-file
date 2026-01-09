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

import java.util.function.IntFunction;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class EntryBaseTest {

    @Test
    void shouldSetAndGetCalc() {
        IntFunction<Integer> calc = i -> i + 1;
        var entry = new Entry("key").calc(calc);
        assertThat(entry.calc()).isEqualTo(calc);
    }

    @Test
    void shouldSetAndGetDefaultValue() {
        var entry = new Entry("key").defaultValue("default");
        assertThat(entry.defaultValue()).isEqualTo("default");
    }

    @Test
    void shouldSetAndGetDelete() {
        var entry = new Entry("key");
        assertThat(entry.isDelete()).isFalse();
        entry.delete();
        assertThat(entry.isDelete()).isTrue();
    }

    @Test
    void shouldSetAndGetKey() {
        var entry = new Entry("key");
        assertThat(entry.key()).isEqualTo("key");
        entry.key("newKey");
        assertThat(entry.key()).isEqualTo("newKey");
    }

    @Test
    void shouldSetAndGetPattern() {
        var entry = new Entry("key").pattern("pattern");
        assertThat(entry.pattern()).isEqualTo("pattern");
    }

    @Test
    void shouldSetAndGetNewValue() {
        var entry = new Entry("key");
        assertThat(entry.newValue()).isNull();
        entry.set("value");
        assertThat(entry.newValue()).isEqualTo("value");
    }

    @Test
    void shouldReturnToString() {
        var entry = new Entry("key").set("value").defaultValue("default");
        assertThat(entry.toString())
                .contains("key='key'")
                .contains("newValue=value")
                .contains("defaultValue=default")
                .contains("EntryBase");
    }
}
