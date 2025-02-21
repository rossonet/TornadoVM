/*
 * Copyright (c) 2013-2020, 2022, APT Group, Department of Computer Science,
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

package uk.ac.manchester.tornado.examples.arrays;

import java.util.Arrays;

import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

/**
 * <p>
 * How to run?
 * </p>
 * <code>
 *     tornado -m tornado.examples/uk.ac.manchester.tornado.examples.arrays.ArrayAccInt
 * </code>
 */
public class ArrayAccInt {

    public static void acc(int[] a, int value) {
        for (@Parallel int i = 0; i < a.length; i++) {
            a[i] += value;
        }
    }

    public static void main(String[] args) {

        final int numElements = 8;
        final int numKernels = 8;
        int[] a = new int[numElements];

        Arrays.fill(a, 10);

        TaskGraph taskGraph = new TaskGraph("s0");

        taskGraph.lockObjectsInMemory(a);
        taskGraph.transferToDevice(DataTransferMode.FIRST_EXECUTION, a);
        for (int i = 0; i < numKernels; i++) {
            taskGraph.task("t" + i, ArrayAccInt::acc, a, 1);
        }
        taskGraph.transferToHost(a);
        taskGraph.execute();

        // The result must be the initial value for the array plus the number of tasks
        // composed in the task-graph.
        System.out.println("a: " + Arrays.toString(a));
    }
}
