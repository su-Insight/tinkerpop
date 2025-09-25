/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal.step.util.event;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.EventStrategy;
import org.apache.tinkerpop.gremlin.structure.*;

public class RemovedEventUtil {

    public static void registerElementRemoval(CallbackRegistry<Event> callbackRegistry,
                                              Traversal.Admin<Object, Object> traversal,
                                              Element elementForRemoval){
        final EventStrategy eventStrategy = traversal.getStrategies().getStrategy(EventStrategy.class).get();
        final Event removeEvent;
        if (elementForRemoval instanceof Vertex)
            removeEvent = new Event.VertexRemovedEvent(eventStrategy.detach((Vertex) elementForRemoval));
        else if (elementForRemoval instanceof Edge)
            removeEvent = new Event.EdgeRemovedEvent(eventStrategy.detach((Edge) elementForRemoval));
        else if (elementForRemoval instanceof VertexProperty)
            removeEvent = new Event.VertexPropertyRemovedEvent(eventStrategy.detach((VertexProperty) elementForRemoval));
        else
            throw new IllegalStateException("The incoming object is not removable: " + elementForRemoval);

        callbackRegistry.getCallbacks().forEach(c -> c.accept(removeEvent));
    }

    public static void registerPropertyRemoval(CallbackRegistry<Event> callbackRegistry,
                                               Traversal.Admin<Object, Object> traversal,
                                               Property elementForRemoval){
        final EventStrategy eventStrategy = traversal.getStrategies().getStrategy(EventStrategy.class).get();
        final Event.ElementPropertyEvent removeEvent;
        if (elementForRemoval.element() instanceof Edge)
            removeEvent = new Event.EdgePropertyRemovedEvent(eventStrategy.detach((Edge) elementForRemoval.element()), eventStrategy.detach(elementForRemoval));
        else if (elementForRemoval.element() instanceof VertexProperty)
            removeEvent = new Event.VertexPropertyPropertyRemovedEvent(eventStrategy.detach((VertexProperty) elementForRemoval.element()), eventStrategy.detach(elementForRemoval));
        else
            throw new IllegalStateException("The incoming object is not removable: " + elementForRemoval);

        callbackRegistry.getCallbacks().forEach(c -> c.accept(removeEvent));
    }

    public static boolean hasAnyCallbacks(CallbackRegistry<Event> callbackRegistry){
        return callbackRegistry != null && !callbackRegistry.getCallbacks().isEmpty();
    }

}
