/*
 * Copyright (c) 2018, 2019, APT Group, School of Computer Science,
 * The University of Manchester. All rights reserved.
 * Copyright (c) 2009, 2017, Oracle and/or its affiliates. All rights reserved.
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
 * Authors: Michalis Papadimitriou
 *
 *
 * */

package uk.ac.manchester.tornado.drivers.opencl.graal.phases;

import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.phases.BasePhase;

import uk.ac.manchester.tornado.drivers.opencl.graal.nodes.LocalArrayAlloc;
import uk.ac.manchester.tornado.runtime.graal.phases.TornadoHighTierContext;

public class TornadoLocalMemAllocation extends BasePhase<TornadoHighTierContext> {

    public TornadoLocalMemAllocation() {

    }

    public void execute(StructuredGraph graph, TornadoHighTierContext context) {
        run(graph, context);
    }

    @Override
    protected void run(StructuredGraph graph, TornadoHighTierContext context) {
        // Prevent Pragma Unroll for non-fpga devices
        LocalArrayAlloc locAlloc = graph.addOrUnique(new LocalArrayAlloc(1024));
        graph.addAfterFixed(graph.start(), locAlloc);
        // boolean peeled;
        // do {
        // peeled = false;
        // final LoopsData dataCounted = new LoopsData(graph);
        // dataCounted.detectedCountedLoops();
        // for (LoopEx loop : dataCounted.countedLoops()) {
        // if (shouldFullUnroll(graph.getOptions(), loop)) {
        // List<EndNode> snapshot = graph.getNodes().filter(EndNode.class).snapshot();
        // int idx = 0;
        // for (EndNode end : snapshot) {
        // idx++;
        // if (idx == 2) {
        // PragmaUnrollNode unroll = graph.addOrUnique(new Pragm(2));
        // graph.addBeforeFixed(end, unroll);
        // // graph.addAfterFixed(StartNode);
        // }
        //
        // }
        // peeled = false;
        // break;
        // }
        // }
        // } while (peeled);

    }

    @Override
    public boolean checkContract() {
        return false;
    }
}
