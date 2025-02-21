/*
 * Copyright (c) 2013-2018, APT Group, School of Computer Science,
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

package uk.ac.manchester.tornado.examples.dynamic;

import java.util.Arrays;

import uk.ac.manchester.tornado.api.Policy;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

/**
 * Simple example to show to dynamic reconfiguration in action.
 * <p>
 * How to run?
 * </p>
 * <code>
 *      tornado -m tornado.examples/uk.ac.manchester.tornado.examples.dynamic.DynamicReconfiguration
 * </code>
 *
 */
public class DynamicReconfiguration {

    public static void saxpy(float alpha, float[] x, float[] y) {
        for (@Parallel int i = 0; i < y.length; i++) {
            y[i] = alpha * x[i];
        }
    }

    public void runWithDynamicProfiler() {
        int numElements = 16777216;
        float[] a = new float[numElements];
        float[] b = new float[numElements];
        Arrays.fill(a, 10);

        new TaskGraph("s0") //
                .transferToDevice(DataTransferMode.FIRST_EXECUTION, a) //
                .task("t0", DynamicReconfiguration::saxpy, 2.0f, a, b) //
                .transferToHost(b) //
                .executeWithProfiler(Policy.PERFORMANCE);

    }

    public static void main(String[] args) {
        new DynamicReconfiguration().runWithDynamicProfiler();
    }
}
