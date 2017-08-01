/* 
 * Copyright 2012 James Clarkson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tornado.drivers.opencl.graal.nodes;

import org.graalvm.compiler.core.common.type.StampFactory;
import org.graalvm.compiler.graph.NodeClass;
import org.graalvm.compiler.nodeinfo.NodeInfo;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.spi.LIRLowerable;
import org.graalvm.compiler.nodes.spi.NodeLIRBuilderTool;
import jdk.vm.ci.meta.JavaKind;
import tornado.drivers.opencl.graal.asm.OCLAssembler.OCLNullaryOp;
import tornado.drivers.opencl.graal.lir.OCLNullary;

@NodeInfo
public class SlotsBaseAddressNode extends ValueNode implements LIRLowerable {

    public static final NodeClass<SlotsBaseAddressNode> TYPE = NodeClass.create(SlotsBaseAddressNode.class);

    public SlotsBaseAddressNode() {
        super(TYPE, StampFactory.forKind(JavaKind.Object));
    }

    @Override
    public void generate(NodeLIRBuilderTool gen) {
        gen.setResult(this, new OCLNullary.Expr(OCLNullaryOp.SLOTS_BASE_ADDRESS, gen.getLIRGeneratorTool().getLIRKind(stamp)));
    }

}
