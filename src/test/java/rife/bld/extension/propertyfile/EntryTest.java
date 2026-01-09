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

import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class EntryTest {

    @Test
    void shouldSetAndGetModify() {
        BiFunction<String, String, String> modify = (s1, s2) -> s1 + s2;
        var entry = new Entry("key").modify(modify);
        assertThat(entry.modify()).isEqualTo(modify);
        assertThat(entry.modifyValue()).isEmpty();
    }

    @Test
    void shouldSetAndGetModifyWithValue() {
        BiFunction<String, String, String> modify = (s1, s2) -> s1 + s2;
        var entry = new Entry("key").modify("value", modify);
        assertThat(entry.modify()).isEqualTo(modify);
        assertThat(entry.modifyValue()).isEqualTo("value");
    }

    @Test
    void shouldSetNewValue() {
        var entry = new Entry("key").set("value");
        assertThat(entry.newValue()).isEqualTo("value");
    }
}
