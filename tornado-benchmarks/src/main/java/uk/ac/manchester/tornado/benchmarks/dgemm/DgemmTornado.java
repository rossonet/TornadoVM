/*
 * Copyright (c) 2013-2022, APT Group, Department of Computer Science,
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
package uk.ac.manchester.tornado.benchmarks.dgemm;

import static uk.ac.manchester.tornado.api.collections.math.TornadoMath.findULPDistance;
import static uk.ac.manchester.tornado.benchmarks.LinearAlgebraArrays.dgemm;

import java.util.Random;

import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.common.Access;
import uk.ac.manchester.tornado.api.common.TornadoDevice;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.api.runtime.TornadoRuntime;
import uk.ac.manchester.tornado.benchmarks.BenchmarkDriver;
import uk.ac.manchester.tornado.benchmarks.LinearAlgebraArrays;

/**
 * <p>
 * How to run?
 * </p>
 * <code>
 *     tornado -m tornado.benchmarks/uk.ac.manchester.tornado.benchmarks.BenchmarkRunner dgemm
 * </code>
 */
public class DgemmTornado extends BenchmarkDriver {

    private final int m;
    private final int n;
    private final boolean USE_PREBUILT = Boolean.parseBoolean(TornadoRuntime.getProperty("usePrebuilt", "False"));
    private double[] a;
    private double[] b;
    private double[] c;

    public DgemmTornado(int iterations, int m, int n) {
        super(iterations);
        this.m = m;
        this.n = n;
    }

    @Override
    public void setUp() {
        a = new double[m * n];
        b = new double[m * n];
        c = new double[m * n];

        final Random random = new Random();

        for (int i = 0; i < m; i++) {
            a[i * (m + 1)] = 1;
        }

        for (int i = 0; i < m * n; i++) {
            b[i] = random.nextFloat();
        }

        taskGraph = new TaskGraph("benchmark");
        if (!USE_PREBUILT) {

            taskGraph.transferToDevice(DataTransferMode.EVERY_EXECUTION, a, b) //
                    .task("dgemm", LinearAlgebraArrays::dgemm, m, n, n, a, b, c) //
                    .transferToHost(c);
            taskGraph.warmup();
        } else {
            String filePath = "/tmp/mxmDouble.spv";
            TornadoDevice device = null;
            int maxDevices = TornadoRuntime.getTornadoRuntime().getDriver(0).getDeviceCount();
            for (int i = 0; i < maxDevices; i++) {
                device = TornadoRuntime.getTornadoRuntime().getDriver(0).getDevice(i);
                if (device.isSPIRVSupported()) {
                    break;
                }
            }

            taskGraph.transferToDevice(DataTransferMode.EVERY_EXECUTION, a, b) //
                    .prebuiltTask("t0", //
                            "dgemm", //
                            filePath, //
                            new Object[] { m, n, n, a, b, c }, //
                            new Access[] { Access.READ, Access.READ, Access.READ, Access.READ, Access.READ, Access.WRITE }, //
                            device, //
                            new int[] { n, n })//
                    .transferToHost(c);//
        }
    }

    @Override
    public void tearDown() {
        taskGraph.dumpProfiles();

        a = null;
        b = null;
        c = null;

        taskGraph.getDevice().reset();
        super.tearDown();
    }

    @Override
    public void benchmarkMethod(TornadoDevice device) {
        taskGraph.mapAllTo(device);
        taskGraph.execute();
    }

    @Override
    public boolean validate(TornadoDevice device) {

        final double[] result = new double[m * n];

        benchmarkMethod(device);
        taskGraph.clearProfiles();

        dgemm(m, n, m, a, b, result);

        final double ulp = findULPDistance(c, result);
        return ulp < MAX_ULP;
    }

    public void printSummary() {
        if (isValid()) {
            System.out.printf("id=%s, elapsed=%f, per iteration=%f\n", TornadoRuntime.getProperty("benchmark.device"), getElapsed(), getElapsedPerIteration());
        } else {
            System.out.printf("id=%s produced invalid result\n", TornadoRuntime.getProperty("benchmark.device"));
        }
    }

}
