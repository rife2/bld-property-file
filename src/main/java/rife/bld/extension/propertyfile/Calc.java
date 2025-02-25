/*
 * Copyright 2023-2025 the original author or authors.
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

import java.util.function.IntFunction;

/**
 * Implements the calculation functions.
 *
 * @author <a href="https://github.com/gbevin">Geert Bevin</a>
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class Calc {
    /**
     * The add function.
     */
    public static final IntFunction<Integer> ADD = Calc::add;
    /**
     * The sub function.
     */
    public static final IntFunction<Integer> SUB = Calc::sub;


    private Calc() {
        // no-op
    }

    /**
     * Adds {@code 1} to the value.
     *
     * @param v the value
     * @return the new value
     */
    public static Integer add(int v) {
        return v + 1;
    }

    /**
     * Subtracts {@code 1} to the value.
     *
     * @param v the value
     * @return the new value
     */
    public static Integer sub(int v) {
        return v - 1;
    }
}

