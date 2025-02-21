/*
 * Copyright (c) 2020, 2022, APT Group, Department of Computer Science,
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
package uk.ac.manchester.tornado.unittests.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Test;

import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;
import uk.ac.manchester.tornado.api.enums.TornadoVMBackendType;
import uk.ac.manchester.tornado.unittests.common.TornadoTestBase;

/**
 * <p>
 * How to test?
 * </p>
 * <code>
 *     tornado-test -V uk.ac.manchester.tornado.unittests.fields.TestFields
 * </code>
 */
public class TestFields extends TornadoTestBase {

    private static class Foo {
        final int[] output;
        final int[] a;
        final int[] b;

        public Foo(int elements) {
            output = new int[elements];
            a = new int[elements];
            b = new int[elements];
        }

        public void initRandom() {
            Random r = new Random();
            IntStream.range(0, a.length).forEach(idx -> {
                a[idx] = r.nextInt(100);
                b[idx] = r.nextInt(100);
            });
        }

        public void computeInit() {
            for (@Parallel int i = 0; i < output.length; i++) {
                output[i] = 100;
            }
        }

        public void computeAdd() {
            for (@Parallel int i = 0; i < output.length; i++) {
                output[i] = a[i] + b[i];
            }
        }
    }

    private static class Bar {
        final int[] output;
        final int initValue;

        public Bar(int elements, int initValue) {
            output = new int[elements];
            this.initValue = initValue;
        }

        public void computeInit() {
            for (@Parallel int i = 0; i < output.length; i++) {
                output[i] = initValue;
            }
        }
    }

    @Test
    public void testFields01() {
        final int N = 1024;
        Foo foo = new Foo(N);

        TaskGraph taskGraph = new TaskGraph("s0");
        assertNotNull(taskGraph);

        taskGraph.lockObjectInMemory(foo);
        taskGraph.task("t0", foo::computeInit);
        taskGraph.execute();

        taskGraph.syncObject(foo.output);
        taskGraph.unlockObjectFromMemory(foo);

        for (int i = 0; i < N; i++) {
            assertEquals(100, foo.output[i]);
        }
    }

    @Test
    public void testFields02() {
        final int N = 1024;
        Foo foo = new Foo(N);
        foo.initRandom();

        TaskGraph taskGraph = new TaskGraph("s0");
        assertNotNull(taskGraph);

        taskGraph.lockObjectInMemory(foo);
        taskGraph.task("t0", foo::computeAdd);
        taskGraph.unlockObjectFromMemory(foo);
        taskGraph.execute();

        taskGraph.syncObject(foo.output);

        for (int i = 0; i < N; i++) {
            assertEquals(foo.a[i] + foo.b[i], foo.output[i]);
        }
    }

    @Test
    public void testFields03() {
        final int N = 1024;
        Bar bar = new Bar(N, 15);

        TaskGraph taskGraph = new TaskGraph("Bar");
        assertNotNull(taskGraph);

        taskGraph.lockObjectInMemory(bar);

        taskGraph.task("init", bar::computeInit).execute();
        taskGraph.syncObject(bar.output);

        taskGraph.unlockObjectFromMemory(bar);

        for (int i = 0; i < N; i++) {
            assertEquals(15, bar.output[i]);
        }
    }

    private static class B {
        final int[] someArray;
        double someField;

        public B() {
            this.someField = -1;
            this.someArray = new int[100];
            Arrays.fill(someArray, -1);
        }
    }

    private static class A {
        private final B b;
        float someOtherField;

        public A(B b) {
            this.b = b;
            someOtherField = -1;
        }
    }

    public static void setField(A a, float value) {
        a.someOtherField = value;
    }

    public static void setNestedField(A a, float value) {
        a.b.someField = value;
    }

    public static void setNestedArray(A a, int[] indexes) {
        for (@Parallel int i = 0; i < indexes.length; i++) {
            a.b.someArray[i] = a.b.someArray[i] + indexes[i] + 3;
        }
    }

    @Test
    public void testSetField() {
        // The reason this is not supported for SPIR-V is that the object fields are
        // deserialized
        // before flushing the command list. Check SPIRVObjectWrapper::deserialise and
        // SPIRVTornadoDevice::flush.
        assertNotBackend(TornadoVMBackendType.SPIRV);

        B b = new B();
        final A a = new A(b);

        TaskGraph taskGraph = new TaskGraph("s0");
        taskGraph.transferToDevice(DataTransferMode.EVERY_EXECUTION, a);
        taskGraph.task("t0", TestFields::setField, a, 77f);
        taskGraph.transferToHost(a);
        taskGraph.execute();

        assertEquals(77, a.someOtherField, 0.01f);
        assertEquals(-1, a.b.someField, 0.01f);
        for (int i = 0; i < b.someArray.length; i++) {
            assertEquals(-1, a.b.someArray[i]);
        }
    }

    @Test
    public void testSetNestedField() {
        // The reason this is not supported for SPIR-V is that the object fields are
        // deserialized
        // before flushing the command list. Check SPIRVObjectWrapper::deserialise and
        // SPIRVTornadoDevice::flush.
        assertNotBackend(TornadoVMBackendType.SPIRV);

        B b = new B();
        final A a = new A(b);

        TaskGraph taskGraph = new TaskGraph("s0");
        taskGraph.transferToDevice(DataTransferMode.EVERY_EXECUTION, a);
        taskGraph.task("t0", TestFields::setNestedField, a, 77f);
        taskGraph.transferToHost(a);
        taskGraph.execute();

        assertEquals(77, a.b.someField, 0.01f);
        assertEquals(-1, a.someOtherField, 0.01f);
        for (int i = 0; i < b.someArray.length; i++) {
            assertEquals(-1, a.b.someArray[i]);
        }
    }

    @Test
    public void testSetNestedArray() {
        B b = new B();
        final A a = new A(b);
        final int[] indexes = new int[b.someArray.length];
        Arrays.fill(indexes, 1);
        Arrays.fill(b.someArray, 2);

        TaskGraph taskGraph = new TaskGraph("s0");
        taskGraph.transferToDevice(DataTransferMode.EVERY_EXECUTION, a);
        taskGraph.task("t0", TestFields::setNestedArray, a, indexes);
        taskGraph.transferToHost(a);
        taskGraph.execute();

        for (int i = 0; i < b.someArray.length; i++) {
            assertEquals(6, a.b.someArray[i]);
        }
        assertEquals(-1, a.someOtherField, 0.01f);
        assertEquals(-1, a.b.someField, 0.01f);
    }

}
