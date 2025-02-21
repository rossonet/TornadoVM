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

package uk.ac.manchester.tornado.examples.reductions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.annotations.Reduce;
import uk.ac.manchester.tornado.api.collections.math.TornadoMath;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

/**
 * <p>
 * How to run?
 * </p>
 * <code>
 *     tornado -m tornado.examples/uk.ac.manchester.tornado.examples.reductions.PiComputation
 * </code>
 *
 */
public class PiComputation {

    public static void computePi(float[] input, @Reduce float[] result) {
        result[0] = 0.0f;
        for (@Parallel int i = 1; i < input.length; i++) {
            float value = input[i] + (float) (TornadoMath.pow(-1, i + 1) / (2 * i - 1));
            result[0] += value;
        }
    }

    public void run(int size) {
        float[] input = new float[size];
        float[] result = new float[1];
        Arrays.fill(result, 0.0f);

        TaskGraph task = new TaskGraph("s0") //
                .transferToDevice(DataTransferMode.EVERY_EXECUTION, input)//
                .task("t0", PiComputation::computePi, input, result) //
                .transferToHost(result);

        ArrayList<Long> timers = new ArrayList<>();
        for (int i = 0; i < ConfigurationReduce.MAX_ITERATIONS; i++) {

            IntStream.range(0, size).sequential().forEach(idx -> {
                input[idx] = 0;
            });

            long start = System.nanoTime();
            task.execute();
            long end = System.nanoTime();

            final float piValue = result[0] * 4;
            System.out.println("PI VALUE: " + piValue);
            timers.add((end - start));
        }

        System.out.println("Median TotalTime: " + Stats.computeMedian(timers));
    }

    public static void main(String[] args) {
        int inputSize = 8192;
        if (args.length > 0) {
            inputSize = Integer.parseInt(args[0]);
        }
        System.out.print("Size = " + inputSize + " ");
        new PiComputation().run(inputSize);
    }
}
