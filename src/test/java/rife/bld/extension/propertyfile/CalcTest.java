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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalcTest {

    @Test
    void add() {
        assertThat(Calc.ADD.apply(1)).isEqualTo(2);
        assertThat(Calc.ADD.apply(-1)).isEqualTo(0);
        assertThat(Calc.ADD.apply(0)).isEqualTo(1);
    }

    @Test
    void addThrowsOnOverflow() {
        assertThatThrownBy(() -> Calc.ADD.apply(Integer.MAX_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void dbl() {
        assertThat(Calc.DOUBLE.apply(2)).isEqualTo(4);
        assertThat(Calc.DOUBLE.apply(-3)).isEqualTo(-6);
        assertThat(Calc.DOUBLE.apply(0)).isZero();
    }

    @Test
    void dblThrowsOnOverflow() {
        assertThatThrownBy(() -> Calc.DOUBLE.apply(Integer.MAX_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void directMethods() {
        assertThat(Calc.add(5)).isEqualTo(6);
        assertThat(Calc.sub(5)).isEqualTo(4);
        assertThat(Calc.dbl(5)).isEqualTo(10);
        assertThat(Calc.half(5)).isEqualTo(2);
        assertThat(Calc.negate(5)).isEqualTo(-5);
    }

    @Test
    void half() {
        assertThat(Calc.HALF.apply(4)).isEqualTo(2);
        assertThat(Calc.HALF.apply(5)).isEqualTo(2); // truncates toward zero
        assertThat(Calc.HALF.apply(-5)).isEqualTo(-2);
        assertThat(Calc.HALF.apply(1)).isZero();
        assertThat(Calc.HALF.apply(0)).isZero();
    }

    @Test
    void negate() {
        assertThat(Calc.NEGATE.apply(5)).isEqualTo(-5);
        assertThat(Calc.NEGATE.apply(-5)).isEqualTo(5);
        assertThat(Calc.NEGATE.apply(0)).isZero();
    }

    @Test
    void negateThrowsOnOverflow() {
        assertThatThrownBy(() -> Calc.NEGATE.apply(Integer.MIN_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void plus() {
        assertThat(Calc.plus(5).apply(10)).isEqualTo(15);
        assertThat(Calc.plus(-3).apply(10)).isEqualTo(7);
        assertThat(Calc.plus(0).apply(42)).isEqualTo(42);
    }

    @Test
    void plusThrowsOnOverflow() {
        assertThatThrownBy(() -> Calc.plus(1).apply(Integer.MAX_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void sub() {
        assertThat(Calc.SUB.apply(1)).isEqualTo(0);
        assertThat(Calc.SUB.apply(0)).isEqualTo(-1);
        assertThat(Calc.SUB.apply(-1)).isEqualTo(-2);
    }

    @Test
    void subThrowsOnOverflow() {
        assertThatThrownBy(() -> Calc.SUB.apply(Integer.MIN_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void times() {
        assertThat(Calc.times(3).apply(4)).isEqualTo(12);
        assertThat(Calc.times(0).apply(100)).isZero();
        assertThat(Calc.times(-2).apply(5)).isEqualTo(-10);
    }

    @Test
    void timesThrowsOnOverflow() {
        assertThatThrownBy(() -> Calc.times(2).apply(Integer.MAX_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }
}