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

import static org.assertj.core.api.Assertions.assertThat;

class EntryIntTest {

    @Test
    void shouldSetPattern() {
        var entry = new EntryInt("key").pattern("000");
        assertThat(entry.pattern()).isEqualTo("000");
    }

    @Test
    void shouldSetInt() {
        var entry = new EntryInt("key").set(42);
        assertThat(entry.newValue()).isEqualTo(42);
    }
}
