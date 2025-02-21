/*
 * Copyright (c) 2013-2020, APT Group, Department of Computer Science,
 * The University of Manchester.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.manchester.tornado.unittests.tasks;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.unittests.common.TornadoTestBase;

/**
 * Testing Tornado with multiple tasks in the same device. The {@link TaskGraph}
 * contains more than one task.
 * <p>
 * How to run?
 * </p>
 * <code>
 *     tornado-test -V uk.ac.manchester.tornado.unittests.tasks.TestMultipleTasksSingleDevice
 * </code>
 *
 */
public class TestMultipleTasksSingleDevice extends TornadoTestBase {

    public static void task0Initialization(int[] a) {
        for (@Parallel int i = 0; i < a.length; i++) {
            a[i] = 10;
        }
    }

    public static void task1Multiplication(int[] a, int alpha) {
        for (@Parallel int i = 0; i < a.length; i++) {
            a[i] = a[i] * alpha;
        }
    }

    public static void task2Saxpy(int[] a, int[] b, int[] c, int alpha) {
        for (@Parallel int i = 0; i < a.length; i++) {
            c[i] = alpha * a[i] + b[i];
        }
    }

    @Test
    public void testTwoTasks() {
        final int numElements = 1024;
        int[] a = new int[numElements];
        int[] b = new int[numElements];

        new TaskGraph("s0")//
                .transferToDevice(DataTransferMode.EVERY_EXECUTION, a, b)//
                .task("t0", TestMultipleTasksSingleDevice::task0Initialization, a)//
                .task("t1", TestMultipleTasksSingleDevice::task1Multiplication, a, 12)//
                .transferToHost(a) //
                .execute();

        for (int j : a) {
            assertEquals(120, j);
        }
    }

    @Test
    public void testThreeTasks() {
        final int numElements = 1024;
        int[] a = new int[numElements];
        int[] b = new int[numElements];

        new TaskGraph("s0")//
                .transferToDevice(DataTransferMode.EVERY_EXECUTION, a, b)//
                .task("t0", TestMultipleTasksSingleDevice::task0Initialization, a)//
                .task("t1", TestMultipleTasksSingleDevice::task1Multiplication, a, 12)//
                .task("t3", TestMultipleTasksSingleDevice::task2Saxpy, a, a, b, 12)//
                .transferToHost(b)//
                .execute();

        int val = (12 * 120) + 120;
        for (int i = 0; i < a.length; i++) {
            assertEquals(val, b[i]);
        }
    }

    @Test
    public void testFourTasks() {
        final int numElements = 1024;
        int[] a = new int[numElements];
        int[] b = new int[numElements];
        int[] c = new int[numElements];

        new TaskGraph("s0")//
                .transferToDevice(DataTransferMode.EVERY_EXECUTION, a, b)//
                .task("t0", TestMultipleTasksSingleDevice::task0Initialization, a)//
                .task("t1", TestMultipleTasksSingleDevice::task1Multiplication, a, 12)//
                .task("t2", TestMultipleTasksSingleDevice::task0Initialization, b)//
                .task("t3", TestMultipleTasksSingleDevice::task2Saxpy, a, b, c, 12)//
                .transferToHost(c)//
                .execute();

        int val = (12 * 120) + 10;
        for (int i = 0; i < a.length; i++) {
            assertEquals(val, c[i]);
        }
    }

    @Test
    public void testFiveTasks() {
        final int numElements = 1024;
        int[] a = new int[numElements];
        int[] b = new int[numElements];
        int[] c = new int[numElements];

        new TaskGraph("s0")//
                .transferToDevice(DataTransferMode.EVERY_EXECUTION, a, b)//
                .task("t0", TestMultipleTasksSingleDevice::task0Initialization, a)//
                .task("t1", TestMultipleTasksSingleDevice::task1Multiplication, a, 12)//
                .task("t2", TestMultipleTasksSingleDevice::task0Initialization, b)//
                .task("t3", TestMultipleTasksSingleDevice::task2Saxpy, a, b, b, 12)//
                .task("t4", TestMultipleTasksSingleDevice::task2Saxpy, b, a, c, 12)//
                .transferToHost(c)//
                .execute();

        int val = (12 * 120) + 10;
        val = (12 * val) + (120);
        for (int i = 0; i < a.length; i++) {
            assertEquals(val, c[i]);
        }
    }

}
