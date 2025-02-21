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

package uk.ac.manchester.tornado.examples.compute;

import java.util.Random;

import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.collections.math.TornadoMath;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

/**
 * Black-Scholes implementation adapted from AMD-OpenCL examples and Marawacc
 * compiler framework.
 * <p>
 * How to run:
 * </p>
 * <code>
 *      tornado -m tornado.examples/uk.ac.manchester.tornado.examples.compute.BlackScholes
 * </code>
 *
 */
public class BlackScholes {

    private static final int WARM_UP_ITERATIONS = 1000;

    private static void blackScholesKernel(float[] input, float[] callResult, float[] putResult) {
        for (@Parallel int idx = 0; idx < callResult.length; idx++) {
            float rand = input[idx];
            final float S_LOWER_LIMIT = 10.0f;
            final float S_UPPER_LIMIT = 100.0f;
            final float K_LOWER_LIMIT = 10.0f;
            final float K_UPPER_LIMIT = 100.0f;
            final float T_LOWER_LIMIT = 1.0f;
            final float T_UPPER_LIMIT = 10.0f;
            final float R_LOWER_LIMIT = 0.01f;
            final float R_UPPER_LIMIT = 0.05f;
            final float SIGMA_LOWER_LIMIT = 0.01f;
            final float SIGMA_UPPER_LIMIT = 0.10f;
            final float S = S_LOWER_LIMIT * rand + S_UPPER_LIMIT * (1.0f - rand);
            final float K = K_LOWER_LIMIT * rand + K_UPPER_LIMIT * (1.0f - rand);
            final float T = T_LOWER_LIMIT * rand + T_UPPER_LIMIT * (1.0f - rand);
            final float r = R_LOWER_LIMIT * rand + R_UPPER_LIMIT * (1.0f - rand);
            final float v = SIGMA_LOWER_LIMIT * rand + SIGMA_UPPER_LIMIT * (1.0f - rand);

            float d1 = (TornadoMath.log(S / K) + ((r + (v * v / 2)) * T)) / v * TornadoMath.sqrt(T);
            float d2 = d1 - (v * TornadoMath.sqrt(T));
            callResult[idx] = S * cnd(d1) - K * TornadoMath.exp(T * (-1) * r) * cnd(d2);
            putResult[idx] = K * TornadoMath.exp(T * -r) * cnd(-d2) - S * cnd(-d1);
        }
    }

    private static float cnd(float X) {
        final float c1 = 0.319381530f;
        final float c2 = -0.356563782f;
        final float c3 = 1.781477937f;
        final float c4 = -1.821255978f;
        final float c5 = 1.330274429f;
        final float zero = 0.0f;
        final float one = 1.0f;
        final float two = 2.0f;
        final float temp4 = 0.2316419f;
        final float oneBySqrt2pi = 0.398942280f;
        float absX = TornadoMath.abs(X);
        float t = one / (one + temp4 * absX);
        float y = one - oneBySqrt2pi * TornadoMath.exp(-X * X / two) * t * (c1 + t * (c2 + t * (c3 + t * (c4 + t * c5))));
        return (X < zero) ? (one - y) : y;
    }

    private static boolean checkResult(float[] call, float[] put, float[] callPrice, float[] putPrice) {
        double delta = 1.8;
        for (int i = 0; i < call.length; i++) {
            if (Math.abs(call[i] - callPrice[i]) > delta) {
                System.out.println("call: " + call[i] + " vs gpu " + callPrice[i]);
                return false;
            }
            if (Math.abs(put[i] - putPrice[i]) > delta) {
                System.out.println("put: " + put[i] + " vs gpu " + putPrice[i]);
                return false;
            }
        }
        return true;
    }

    public static void blackScholes(int size) {
        Random random = new Random();
        float[] input = new float[size];
        float[] callPrice = new float[size];
        float[] putPrice = new float[size];
        float[] seqCall = new float[size];
        float[] seqPut = new float[size];
        for (int i = 0; i < size; i++) {
            input[i] = random.nextFloat();
        }

        TaskGraph taskGraph = new TaskGraph("s0") //
                .transferToDevice(DataTransferMode.EVERY_EXECUTION, input) //
                .task("t0", BlackScholes::blackScholesKernel, input, callPrice, putPrice) //
                .transferToHost(callPrice, putPrice);

        for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
            taskGraph.execute();
        }
        long start = System.nanoTime();
        taskGraph.execute();
        long end = System.nanoTime();

        for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
            blackScholesKernel(input, seqCall, seqPut);
        }
        long start2 = System.nanoTime();
        blackScholesKernel(input, seqCall, seqPut);
        long end2 = System.nanoTime();

        boolean results = checkResult(seqCall, seqPut, callPrice, putPrice);

        System.out.println("Validation " + results + " \n");
        System.out.println("Seq     : " + (end2 - start2) + " (ns)");
        System.out.println("Parallel: " + (end - start) + " (ns)");
        System.out.println("Speedup : " + ((end2 - start2) / (end - start)) + "x");
    }

    public static void main(String[] args) {
        System.out.println("BlackScholes TornadoVM");
        int size = 1024;
        if (args.length > 0) {
            try {
                size = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        System.out.println("Input size: " + size + " \n");
        blackScholes(size);
    }
}
