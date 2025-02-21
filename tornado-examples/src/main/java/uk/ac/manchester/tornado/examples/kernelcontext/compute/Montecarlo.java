/*
 * Copyright (c) 2021-2022 APT Group, Department of Computer Science,
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

package uk.ac.manchester.tornado.examples.kernelcontext.compute;

import uk.ac.manchester.tornado.api.GridScheduler;
import uk.ac.manchester.tornado.api.KernelContext;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.WorkerGrid;
import uk.ac.manchester.tornado.api.WorkerGrid1D;

/**
 * Montecarlo algorithm to approximate the PI value. This version has been
 * adapted from Marawacc test-suite.
 * <p>
 * How to run?
 * </p>
 * <code>
 *     tornado -m tornado.examples/uk.ac.manchester.tornado.examples.kernelcontext.compute.Montecarlo
 * </code>
 *
 */
public class Montecarlo {

    private static void computeMontecarlo(KernelContext context, float[] output, final int iterations) {
        int j = context.globalIdx;

        long seed = j;
        // generate a pseudo random number (you do need it twice)
        seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);

        // this generates a number between 0 and 1 (with an awful entropy)
        float x = (seed & 0x0FFFFFFF) / 268435455f;

        // repeat for y
        seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        float y = (seed & 0x0FFFFFFF) / 268435455f;

        float dist = (float) Math.sqrt(x * x + y * y);
        if (dist <= 1.0f) {
            output[j] = 1.0f;
        } else {
            output[j] = 0.0f;
        }
    }

    private static void computeMontecarlo(float[] output, final int iterations) {
        for (int j = 0; j < iterations; j++) {
            long seed = j;
            // generate a pseudo random number (you do need it twice)
            seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);

            // this generates a number between 0 and 1 (with an awful entropy)
            float x = (seed & 0x0FFFFFFF) / 268435455f;

            // repeat for y
            seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            float y = (seed & 0x0FFFFFFF) / 268435455f;

            float dist = (float) Math.sqrt(x * x + y * y);
            if (dist <= 1.0f) {
                output[j] = 1.0f;
            } else {
                output[j] = 0.0f;
            }
        }
    }

    public static void montecarlo(final int size) {
        float[] output = new float[size];
        float[] seq = new float[size];

        WorkerGrid workerGrid = new WorkerGrid1D(size);
        GridScheduler gridScheduler = new GridScheduler("s0.t0", workerGrid);
        KernelContext context = new KernelContext();
        // [Optional] Set the global work size
        workerGrid.setGlobalWork(size, 1, 1);
        // [Optional] Set the local work group to be 1024, 1, 1
        workerGrid.setLocalWork(1024, 1, 1);

        TaskGraph t0 = new TaskGraph("s0").task("t0", Montecarlo::computeMontecarlo, context, output, size).transferToHost(output);

        long start = System.nanoTime();
        t0.execute(gridScheduler);
        long end = System.nanoTime();
        long tornadoTime = (end - start);

        float sum = 0;
        for (int j = 0; j < size; j++) {
            sum += output[j];
        }
        sum *= 4;
        System.out.println("Total time (Tornado)   : " + (tornadoTime));
        System.out.println("Pi value(Tornado)   : " + (sum / size));

        start = System.nanoTime();
        computeMontecarlo(seq, size);
        end = System.nanoTime();
        long sequentialTime = (end - start);

        sum = 0;
        for (int j = 0; j < size; j++) {
            sum += seq[j];
        }
        sum *= 4;

        System.out.println("Total time (Sequential): " + (sequentialTime));
        System.out.println("Pi value(seq)   : " + (sum / size));

        double speedup = (double) sequentialTime / (double) tornadoTime;
        System.out.println("Speedup: " + speedup);
    }

    public static void main(String[] args) {
        System.out.println("Compute Montecarlo");
        montecarlo(16777216);
    }

}
