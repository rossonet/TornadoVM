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
package tornado.runtime.sketcher;

import org.graalvm.compiler.phases.PhaseSuite;
import org.graalvm.compiler.phases.tiers.HighTierContext;
import org.graalvm.compiler.phases.util.Providers;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import tornado.api.meta.TaskMetaData;
import tornado.graal.compiler.TornadoSketchTier;

import static tornado.common.exceptions.TornadoInternalError.unimplemented;

public class SketchRequest implements Future<Sketch>, Runnable {

    public final TaskMetaData meta;
    public final ResolvedJavaMethod resolvedMethod;
    public final Providers providers;
    public final PhaseSuite<HighTierContext> graphBuilderSuite;
    public final TornadoSketchTier sketchTier;
    public Sketch result;

    public SketchRequest(TaskMetaData meta, ResolvedJavaMethod resolvedMethod, Providers providers, PhaseSuite<HighTierContext> graphBuilderSuite, TornadoSketchTier sketchTier) {
        this.resolvedMethod = resolvedMethod;
        this.providers = providers;
        this.graphBuilderSuite = graphBuilderSuite;
        this.sketchTier = sketchTier;
        this.meta = meta;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public void run() {
        TornadoSketcher.buildSketch(this);
    }

    @Override
    public Sketch get() throws InterruptedException, ExecutionException {
        while (!isDone()) {
            Thread.sleep(100);
        }
        return result;
    }

    @Override
    public Sketch get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        unimplemented();
        return null;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return result != null;
    }
}
