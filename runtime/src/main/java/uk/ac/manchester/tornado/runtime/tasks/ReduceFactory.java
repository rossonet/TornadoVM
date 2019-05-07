/*
 * This file is part of Tornado: A heterogeneous programming framework: 
 * https://github.com/beehive-lab/tornado
 *
 * Copyright (c) 2013-2019, APT Group, School of Computer Science,
 * The University of Manchester. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package uk.ac.manchester.tornado.runtime.tasks;

import uk.ac.manchester.tornado.api.TaskSchedule;
import uk.ac.manchester.tornado.api.exceptions.TornadoRuntimeException;

class ReduceFactory {

    private static void rAdd(int[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] += array[i];
        }
    }

    private static void rAdd(float[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] += array[i];
        }
    }

    private static void rAdd(double[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] += array[i];
        }
    }

    private static void rMul(int[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] *= array[i];
        }
    }

    private static void rMul(float[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] *= array[i];
        }
    }

    private static void rMul(double[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] *= array[i];
        }
    }

    private static void rMax(int[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] = Math.max(array[0], array[i]);
        }
    }

    private static void rMax(float[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] = Math.max(array[0], array[i]);
        }
    }

    private static void rMax(double[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] = Math.max(array[0], array[i]);
        }
    }

    private static void rMin(int[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] = Math.min(array[0], array[i]);
        }
    }

    private static void rMin(float[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] = Math.min(array[0], array[i]);
        }
    }

    private static void rMin(double[] array, final int size) {
        for (int i = 1; i < size; i++) {
            array[0] = Math.min(array[0], array[i]);
        }
    }

    static void handleAdd(Object newArray, TaskSchedule task, int sizeReduceArray) {
        switch (newArray.getClass().getTypeName()) {
            case "int[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rAdd, (int[]) newArray, sizeReduceArray);
                break;
            case "float[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rAdd, (float[]) newArray, sizeReduceArray);
                break;
            case "double[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rAdd, (double[]) newArray, sizeReduceArray);
                break;
            default:
                throw new TornadoRuntimeException("[ERROR] Reduce data type not supported yet: " + newArray.getClass().getTypeName());
        }
    }

    static void handleMul(Object newArray, TaskSchedule task, int sizeReduceArray) {
        switch (newArray.getClass().getTypeName()) {
            case "int[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMul, (int[]) newArray, sizeReduceArray);
                break;
            case "float[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMul, (float[]) newArray, sizeReduceArray);
                break;
            case "double[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMul, (double[]) newArray, sizeReduceArray);
                break;
            default:
                throw new TornadoRuntimeException("[ERROR] Reduce data type not supported yet: " + newArray.getClass().getTypeName());
        }
    }

    static void handleMax(Object newArray, TaskSchedule task, int sizeReduceArray) {
        switch (newArray.getClass().getTypeName()) {
            case "int[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMax, (int[]) newArray, sizeReduceArray);
                break;
            case "float[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMax, (float[]) newArray, sizeReduceArray);
                break;
            case "double[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMax, (double[]) newArray, sizeReduceArray);
                break;
            default:
                throw new TornadoRuntimeException("[ERROR] Reduce data type not supported yet: " + newArray.getClass().getTypeName());
        }
    }

    static void handleMin(Object newArray, TaskSchedule task, int sizeReduceArray) {
        switch (newArray.getClass().getTypeName()) {
            case "int[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMin, (int[]) newArray, sizeReduceArray);
                break;
            case "float[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMin, (float[]) newArray, sizeReduceArray);
                break;
            case "double[]":
                task.task(ReduceTaskSchedule.SEQUENTIAL_TASK_REDUCE_NAME, ReduceFactory::rMin, (double[]) newArray, sizeReduceArray);
                break;
            default:
                throw new TornadoRuntimeException("[ERROR] Reduce data type not supported yet: " + newArray.getClass().getTypeName());
        }
    }
}
