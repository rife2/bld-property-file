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

import java.util.function.IntFunction;

/**
 * Implements the calculation functions for integer properties.
 * <p>
 * {@link #ADD} and {@link #SUB} match Apache Ant {@code <propertyfile>} behavior.
 * Additional functions are extensions.
 *
 * @author <a href="https://github.com/gbevin">Geert Bevin</a>
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class Calc {

    /**
     * Adds {@code 1}. Ant-compatible.
     */
    public static final IntFunction<Integer> ADD = Calc::add;
    /**
     * Multiplies by {@code 2}. Extension.
     */
    public static final IntFunction<Integer> DOUBLE = Calc::dbl;
    /**
     * Divides by {@code 2}. Extension.
     */
    public static final IntFunction<Integer> HALF = Calc::half;
    /**
     * Negates the value. Extension.
     */
    public static final IntFunction<Integer> NEGATE = Calc::negate;
    /**
     * Subtracts {@code 1}. Ant-compatible.
     */
    public static final IntFunction<Integer> SUB = Calc::sub;

    private Calc() {
        // no-op
    }

    /**
     * Adds {@code 1} to the value.
     */
    public static Integer add(int v) {
        return Math.addExact(v, 1);
    }

    /**
     * Doubles the value.
     */
    public static Integer dbl(int v) {
        return Math.multiplyExact(v, 2);
    }

    /**
     * Halves the value, truncating toward zero.
     */
    public static Integer half(int v) {
        return v / 2;
    }

    /**
     * Negates the value.
     */
    public static Integer negate(int v) {
        return Math.negateExact(v);
    }

    /**
     * Returns a function that adds a constant.
     *
     * @param n the constant to add
     * @return a function adding {@code n}
     */
    public static IntFunction<Integer> plus(int n) {
        return v -> Math.addExact(v, n);
    }

    /**
     * Subtracts {@code 1} from the value.
     */
    public static Integer sub(int v) {
        return Math.subtractExact(v, 1);
    }

    /**
     * Returns a function that multiplies by a constant.
     *
     * @param n the constant to multiply by
     * @return a function multiplying by {@code n}
     */
    public static IntFunction<Integer> times(int n) {
        return v -> Math.multiplyExact(v, n);
    }
}